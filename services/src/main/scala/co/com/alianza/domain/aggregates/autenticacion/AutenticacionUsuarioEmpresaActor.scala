package co.com.alianza.domain.aggregates.autenticacion

import java.sql.{Time, Timestamp}
import java.util.{Calendar, Date}

import akka.actor.ActorLogging
import akka.pattern.ask
import akka.util.Timeout

import co.com.alianza.app.MainActors
import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.constants.TiposConfiguracion
import co.com.alianza.domain.aggregates.autenticacion.errores._
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.anticorruption.usuarios.{DataAccessAdapter => UsDataAdapter}
import co.com.alianza.infrastructure.anticorruption.empresa.{DataAccessTranslator => EmpDataAccessTranslator}
import co.com.alianza.infrastructure.dto._
import co.com.alianza.infrastructure.messages._

import co.com.alianza.persistence.entities.ReglasContrasenas

import co.com.alianza.util.token.Token
import co.com.alianza.util.transformers.ValidationT

import java.sql.Timestamp
import java.util.Date

import enumerations.EstadosEmpresaEnum
import enumerations.empresa.EstadosDeEmpresaEnum

import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.{Success => sSuccess, Failure => sFailure}

import spray.http.StatusCodes._

import scalaz.std.AllInstances._
import scalaz.{Failure => zFailure, Success => zSuccess, Validation}
import scalaz.Validation.FlatMap._

class AutenticacionUsuarioEmpresaActor extends AutenticacionActor with ActorLogging {

  import context.dispatcher

  implicit val timeout: Timeout = Timeout(10 seconds)

  override def receive = {

    /**
     * Flujo:
     * Se consulta si el usuario a autenticar es un agente empresarial, de ser así se manda un mensaje el mismo para autenticar ese tipo de usuario
     * Si no se cumple lo anterior se valida si el usuario es cliente administrador, si es asi se manda un mensaje el mismo para autenticar ese tipo de usuario
     * Si no se cumple ninguna de las dos cosas se retorna ErrorCredencialesInvalidas
     */
    case message: AutenticarUsuarioEmpresarialMessage =>
      val originalSender = sender()

      MainActors.usuariosActorSupervisor ? ConsultaUsuarioEmpresarialMessage(usuario = Some(message.usuario), nit = Some(message.nit)) onComplete {
        case sSuccess(Some(usuarioEmpresarialAgente)) =>
          self tell(AutenticarUsuarioEmpresarialAgenteMessage(message.tipoIdentificacion, message.numeroIdentificacion, message.nit, message.usuario, message.password, message.clientIp), originalSender)
        case sSuccess(None) =>
          MainActors.usuariosActorSupervisor ? ConsultaUsuarioEmpresarialAdminMessage(usuario = Some(message.usuario), nit = Some(message.nit)) onComplete {
            case sSuccess(Some(usuarioEmpresarialAdmin)) =>
              self tell(AutenticarUsuarioEmpresarialAdminMessage(message.tipoIdentificacion, message.numeroIdentificacion, message.nit, message.usuario, message.password, message.clientIp), originalSender)
            case sSuccess(None) =>
              originalSender ! ResponseMessage(Unauthorized, ErrorCredencialesInvalidas().msg)
            case sFailure(t) =>
              originalSender ! t
          }
        case sFailure(t) =>
          originalSender ! t
      }


    /**
     * Flujo:
     * 1) Busca el usuario administrados en la base de datos, si no se encuentra se devuelve CredencialesInvalidas
     * 2) Valida los estados del usuario encontrado, esta validacion devuelve un tipo de error por estado, si es exitosa se continúa el proceso
     * 3) Se comparan los passwords de la petición y el usuario, si coinciden se prosigue de lo contrario se debe ejecutar la excepcion de pw inválido
     * 4) Se busca el cliente en el core de alianza, si no se encuentra se debe devolver ErrorClienteNoExisteCore
     * 5) Se valida el cliente encontrado, este metodo devuelve un error de la validacion que no cumple
     * 6) Se valida la fecha de caducacion del password, si caducó se debe devolver ErrorPasswordCaducado, de lo contrario se prosigue
     * ------- Si pasan las 6 validaciones anteriores, el usuario se considera como usuario autenticado --------
     * 7) Se actualiza la información de numIngresosErroneos, ipUltimoIngreso y fechaUltimoIngreso del usuario
     * 8) Se genera un token y se asocia al usuario
     * 9) Se crea la sesion del usuario en el cluster
     * 10) Se valida si el usuario tiene alguna ip guardada, si es así se procede a validar si es una ip habitual, de lo contrario se genera un token (10), una sesion (11) y se responde con ErrorControlIpsDesactivado
     */
    case message: AutenticarUsuarioEmpresarialAdminMessage =>
      val originalSender = sender()

      val validaciones: Future[Validation[ErrorAutenticacion, String]] = (for {
        estadoEmpresaOk <- ValidationT(validarEstadoEmpresa(message.nit))
        usuarioAdmin <- ValidationT(obtenerUsuarioEmpresarialAdmin(message.nit, message.usuario))
        estadoValido <- ValidationT(validarEstadosUsuario(usuarioAdmin.estado))
        passwordValido <- ValidationT(validarPasswords(message.password, usuarioAdmin.contrasena.getOrElse(""), None, Some(usuarioAdmin.id), usuarioAdmin.numeroIngresosErroneos ))
        cliente <- ValidationT(obtenerClienteSP(usuarioAdmin.tipoIdentificacion, usuarioAdmin.identificacion))
        cienteValido <- ValidationT(validarClienteSP(usuarioAdmin.tipoIdentificacion, cliente))
        passwordCaduco <- ValidationT(validarCaducidadPassword(TiposCliente.clienteAdministrador, usuarioAdmin.id, usuarioAdmin.fechaCaducidad))
        actualizacionInfo <- ValidationT(actualizarInformacionUsuarioEmpresarialAdmin(usuarioAdmin.id, message.clientIp.get))
        inactividadConfig <- ValidationT(buscarConfiguracion(TiposConfiguracion.EXPIRACION_SESION.llave))
        token <- ValidationT(generarYAsociarTokenUsuarioEmpresarialAdmin(cliente, usuarioAdmin, message.nit, inactividadConfig.valor))
        sesion <- ValidationT(crearSesion(token, inactividadConfig.valor.toInt))
        empresa <- ValidationT(obtenerEmpresaPorNit(message.nit))
        horario           <- ValidationT(obtenerHorarioEmpresa(empresa.id))
        horarioValido     <- ValidationT(validarHorarioEmpresa(horario))
        validacionIps <- ValidationT(validarControlIpsUsuarioEmpresarial(empresa.id, message.clientIp.get, token))
      } yield validacionIps).run

      validaciones.onComplete{
        case sFailure(_) => originalSender ! _
        case sSuccess(resp) => resp match {
          case zSuccess(token) => originalSender ! token
          case zFailure(errorAutenticacion) => errorAutenticacion match {
            case err @ ErrorPersistencia(_, ep1) => originalSender ! ep1
            case err @ ErrorPasswordInvalido(_, idUsuario, numIngresosErroneosUsuario) =>

              val ejecucion: Future[Validation[ErrorAutenticacion, Boolean]] = (for {
                ingresosErroneos <- ValidationT(actualizarIngresosErroneosUsuarioEmpresarialAdmin(idUsuario.get, numIngresosErroneosUsuario + 1))
                regla <- ValidationT(buscarRegla("CANTIDAD_REINTENTOS_INGRESO_CONTRASENA"))
                bloqueo <- ValidationT(bloquearUsuarioEmpresarialAdmin(idUsuario.get, numIngresosErroneosUsuario, regla))
              } yield bloqueo).run

              ejecucion.onFailure { case _ => originalSender ! _ }
              ejecucion.onSuccess {
                case zSuccess(_) => originalSender ! ResponseMessage(Unauthorized, err.msg)
                case zFailure(errorBloqueo) => errorBloqueo match {
                  case errb @ ErrorPersistencia(_, ep2) => originalSender ! ep2
                  case _ => originalSender ! ResponseMessage(Unauthorized, errorBloqueo.msg)
                }
              }

            case _ => originalSender ! ResponseMessage(Unauthorized, errorAutenticacion.msg)
          }
        }
      }

    /**
     * Flujo:
     * 1) Busca el usuario en la base de datos, si no se encuentra se devuelve CredencialesInvalidas
     * 2) Valida los estados del usuario encontrado, esta validacion devuelve un tipo de error por estado, si es exitosa se continúa el proceso
     * 3) Se comparan los passwords de la petición y el usuario, si coinciden se prosigue de lo contrario se debe ejecutar la excepcion de pw inválido
     * 4) Se valida la fecha de caducacion del password, si caducó se debe devolver ErrorPasswordCaducado, de lo contrario se prosigue
     * ------- Si pasan las 4 validaciones anteriores, el usuario se considera como usuario autenticado --------
     * 5) Se actualiza la información de numIngresosErroneos, ipUltimoIngreso y fechaUltimoIngreso del usuario
     * 6) Se busca el tiempo de expiración de la sesión
     * 7) Se genera un token y se asocia al usuario
     * 8) Se busca la empresa a la que pertenece el agente
     * 9) Se busca la configuración de horario de la empresa
     * 10)Se valida que el horario corresponda
     * 11)Se crea la sesion del usuario en el cluster
     * 12)Se valida que el usuario ingrese con una ip valida para la empresa a la que pertenece
     */
    case message: AutenticarUsuarioEmpresarialAgenteMessage =>
      val originalSender = sender()

      def validaciones: Future[Validation[ErrorAutenticacion, String]] = (for {
        estadoEmpresaOk <- ValidationT(validarEstadoEmpresa(message.nit))
        usuarioAgente     <- ValidationT(obtenerUsuarioEmpresarialAgente(message.nit, message.usuario))
        estadoValido      <- ValidationT(validarEstadosUsuario(usuarioAgente.estado))
        passwordValido    <- ValidationT(validarPasswords(message.password, usuarioAgente.contrasena.getOrElse(""), None, Some(usuarioAgente.id), usuarioAgente.numeroIngresosErroneos))
        passwordCaduco    <- ValidationT(validarCaducidadPassword(TiposCliente.agenteEmpresarial, usuarioAgente.id, usuarioAgente.fechaCaducidad))
        actualizacionInfo <- ValidationT(actualizarInformacionUsuarioEmpresarialAgente(usuarioAgente.id, message.clientIp.get))
        inactividadConfig <- ValidationT(buscarConfiguracion(TiposConfiguracion.EXPIRACION_SESION.llave))
        token      			  <- ValidationT(generarYAsociarTokenUsuarioEmpresarialAgente(usuarioAgente, message.nit, inactividadConfig.valor))
        empresa 	    	  <- ValidationT(obtenerEmpresaPorNit(message.nit))
		    horario           <- ValidationT(obtenerHorarioEmpresa(empresa.id))
        horarioValido     <- ValidationT(validarHorarioEmpresa(horario))
        sesion 			      <- ValidationT(crearSesion(token, inactividadConfig.valor.toInt, empresa))
        validacionIps 	  <- ValidationT(validarControlIpsUsuarioEmpresarial(empresa.id, message.clientIp.get, token))
      } yield validacionIps).run

      validaciones.onComplete {
        case sFailure(_) => originalSender ! _
        case sSuccess(resp) => resp match {
          case zSuccess(token) => originalSender ! token
          case zFailure(errorAutenticacion) => errorAutenticacion match {
            case err @ ErrorPersistencia(_, ep1) => originalSender ! ep1
            case err @ ErrorPasswordInvalido(_, idUsuario, numIngresosErroneosUsuario) =>

              val ejecucion: Future[Validation[ErrorAutenticacion, Boolean]] = (for {
                ingresosErroneos <- ValidationT(actualizarIngresosErroneosUsuarioEmpresarialAgente(idUsuario.get, numIngresosErroneosUsuario + 1))
                regla <- ValidationT(buscarRegla("CANTIDAD_REINTENTOS_INGRESO_CONTRASENA"))
                bloqueo <- ValidationT(bloquearUsuarioEmpresarialAgente(idUsuario.get, numIngresosErroneosUsuario, regla))
              } yield bloqueo).run

              ejecucion.onFailure { case _ => originalSender ! _ }
              ejecucion.onSuccess {
                case zSuccess(_) => originalSender ! ResponseMessage(Unauthorized, err.msg)
                case zFailure(errorBloqueo) => errorBloqueo match {
                  case errb @ ErrorPersistencia(_, ep2) => originalSender ! ep2
                  case _ => originalSender ! ResponseMessage(Unauthorized, errorBloqueo.msg)
                }
              }

            case _ => originalSender ! ResponseMessage(Unauthorized, errorAutenticacion.msg)
          }
        }
      }

    /**
     * Flujo:
     * 1) Se busca el usuario por id si no se encuentra se devuelve CredencialesInvalidas
     * 2) Se busca el usuario en el core de alianza si no se encuentra se deuvleve ClienteNoExisteEnCore
     * 3) Se valida el estado del cliente en el core
     * 4) Se relaciona la ip con el id del usuario
     */
    case message: AgregarIPHabitualUsuarioEmpresarialAdmin =>
      val originalSender = sender()

      val resultadoIp: Future[Validation[ErrorAutenticacion, Boolean]] = (for {
        usuarioAdmin <- ValidationT(obtenerUsuarioEmpresarialAdmin(message.idUsuario.get))
        cliente <- ValidationT(obtenerClienteSP(usuarioAdmin.tipoIdentificacion, usuarioAdmin.identificacion))
        cienteValido <- ValidationT(validarClienteSP(usuarioAdmin.tipoIdentificacion, cliente))
        idEmpresa <- ValidationT(obtenerEmpresaIdUsuarioAdmin(message.idUsuario.get))
        relacionarIp <- ValidationT(asociarIpEmpresa(idEmpresa, message.clientIp.get))
      } yield relacionarIp).run

      resultadoIp.onComplete {
        case sFailure(ex) => originalSender ! ex
        case sSuccess(resp) =>
          resp match {
            case zFailure(errorValidacion) => originalSender ! ResponseMessage(Unauthorized, errorValidacion.msg)
            case zSuccess(_) => originalSender ! "Registro de IP Exitoso"
          }
      }

  }

  //----------------
  // IPS ADMIN
  //----------------

  /**
   * Se obtiene el usuario empresarial admin por id
   * @param id Id del usuario
   * @return Future[Validation[ErrorAutenticacion, UsuarioEmpresarialAdmin] ]
   * Success => UsuarioEmpresarialAdmin
   * ErrorAutenticacion => ErrorPersistencia || ErrorCredencialesInvalidas
   */
  def obtenerUsuarioEmpresarialAdmin(id: Int): Future[Validation[ErrorAutenticacion, UsuarioEmpresarialAdmin]] = {
    log.info("Obteniendo usuario empresarial admin")
    val future: Future[Validation[PersistenceException, Option[UsuarioEmpresarialAdmin]]] = UsDataAdapter.obtenerUsuarioEmpresarialAdminPorId(id)
    future.map(_.leftMap(pe => ErrorPersistencia(pe.message, pe)).flatMap {
      case Some(usuarioEmpresarialAdmin) => Validation.success(usuarioEmpresarialAdmin)
      case None => Validation.failure(ErrorCredencialesInvalidas())
    })
  }

  def obtenerEmpresaPorNit(nit: String): Future[Validation[ErrorAutenticacion, Empresa]] ={
    log.info("Obteniendo empresa por nit")
    val future : Future[Validation[PersistenceException, Option[Empresa]]] = UsDataAdapter.obtenerEmpresaPorNit(nit)
    future.map(
      _.leftMap(pe => ErrorPersistencia(pe.message, pe)).flatMap{
        case Some(empresa) => Validation.success(empresa)
        case None => Validation.failure(ErrorCredencialesInvalidas())
      }
    )
  }

  def obtenerEmpresaIdUsuarioAdmin(idUsuario: Int): Future[Validation[ErrorAutenticacion, Int]] ={
    log.info("Obteniendo empresa por idUsuario empresarial Admin")
    val future : Future[Validation[PersistenceException, Int]] = UsDataAdapter.obtenerIdEmpresa(idUsuario, TiposCliente.clienteAdministrador)
    future.map(
      _.leftMap(pe => ErrorPersistencia(pe.message, pe)).flatMap{
        idEmpresa => Validation.success(idEmpresa)
      }
    )
  }

  /**
   * Asocia la ip al usuario empresarial admin
   * @param idEmpresa id de la empresa
   * @param ipPeticion ip a asociar
   * @return Future[Validation[ErrorAutenticacion, Boolean] ]
   * Success => True
   * ErrorAutenticacion => PersistenceException
   */
  def asociarIpEmpresa(idEmpresa: Int, ipPeticion: String): Future[Validation[ErrorAutenticacion, Boolean]] = {
    log.info("Asociando ip empresa")
    val future: Future[Validation[PersistenceException, String]] = UsDataAdapter.relacionarIpEmpresa(idEmpresa, ipPeticion)
    future.map(_.leftMap(pe => ErrorPersistencia(pe.message, pe)).flatMap { _ =>
      Validation.success(true)
    })
  }

  //----------------
  // IPS AGENTE
  //----------------

  /**
   * Se obtiene el usuario empresarial agente por id
   * @param id Id del usuario
   * @return Future[Validation[ErrorAutenticacion, UsuarioEmpresarial] ]
   * Success => UsuarioEmpresarial
   * ErrorAutenticacion => ErrorPersistencia || ErrorCredencialesInvalidas
   */
  def obtenerUsuarioEmpresarialAgente(id: Int): Future[Validation[ErrorAutenticacion, UsuarioEmpresarial]] = {
    log.info("Obteniendo usuario empresarial agente")
    val future: Future[Validation[PersistenceException, Option[UsuarioEmpresarial]]] = UsDataAdapter.obtenerUsuarioEmpresarialAgentePorId(id)
    future.map(_.leftMap(pe => ErrorPersistencia(pe.message, pe)).flatMap {
      case Some(usuarioEmpresarialAgente) => Validation.success(usuarioEmpresarialAgente)
      case None => Validation.failure(ErrorCredencialesInvalidas())
    })
  }

  //----------------
  // VALIDACIONES AUTENTICACIÓN ADMIN
  //----------------

  /**
   * Se obtiene el usuario empresarial admin por identificacion
   * @param nit Nit del usuario
   * @param usuario Nombre de usuario
   * @return Future[Validation[ErrorAutenticacion, UsuarioEmpresarialAdmin] ]
   * Success => UsuarioEmpresarialAdmin
   * ErrorAutenticacion => ErrorPersistencia || ErrorCredencialesInvalidas
   */
  def obtenerUsuarioEmpresarialAdmin(nit: String, usuario: String): Future[Validation[ErrorAutenticacion, UsuarioEmpresarialAdmin]] = {
    log.info("Obteniendo usuario empresarial admin")
    val future: Future[Validation[PersistenceException, Option[UsuarioEmpresarialAdmin]]] = UsDataAdapter.obtieneUsuarioEmpresarialAdminPorNitYUsuario(nit, usuario)
    future.map(_.leftMap(pe => ErrorPersistencia(pe.message, pe)).flatMap {
      case Some(usuarioEmpresarialAdmin) => Validation.success(usuarioEmpresarialAdmin)
      case None => Validation.failure(ErrorCredencialesInvalidas())
    })
  }

  /**
   * Actualiza la informacion de inicio de sesion del usuario empresarial admin
   * @param idUsuario Id del usuario
   * @param ipPeticion Ip de la peticion
   * @return Future[Validation[ErrorAutenticacion, Boolean] ]
   * Success => True
   * ErrorAutenticacion => ErrorPersistencia
   */
  def actualizarInformacionUsuarioEmpresarialAdmin(idUsuario: Int, ipPeticion: String): Future[Validation[ErrorAutenticacion, Boolean]] = {
    log.info("Actualizando informacion del usuario empresarial admin")
    (for {
      ingErroneos <- ValidationT(UsDataAdapter.actualizarNumeroIngresosErroneosUsuarioEmpresarialAdmin(idUsuario, 0))
      ipUltimoIngreso <- ValidationT(UsDataAdapter.actualizarIpUltimoIngresoUsuarioEmpresarialAdmin(idUsuario, ipPeticion))
      fechaUltimoIngreso <- ValidationT(UsDataAdapter.actualizarFechaUltimoIngresoUsuarioEmpresarialAdmin(idUsuario, new Timestamp((new Date).getTime)))
    } yield {
      true
    }).run.map(_.leftMap(pe => ErrorPersistencia(pe.message, pe)))
  }

  /**
   * Genera un token y lo asocia al usuario empresarial admin
   * @param cliente cliente del core de alianza
   * @param usuario Usuario autenticado
   * @return Future[Validation[ErrorAutenticacion, String] ]
   * Success => Token generado y asociado
   * ErrorAutenticacion => ErrorPersistencia
   */
  def generarYAsociarTokenUsuarioEmpresarialAdmin(cliente: Cliente, usuario: UsuarioEmpresarialAdmin, nit: String, expiracionInactividad: String): Future[Validation[ErrorAutenticacion, String]] = {
    log.info("Generando y asociando token usuario empresarial admin")
    val token: String = Token.generarToken(cliente.wcli_nombre, cliente.wcli_dir_correo, cliente.wcli_person, usuario.ipUltimoIngreso.getOrElse(""), usuario.fechaUltimoIngreso.getOrElse(new Date(System.currentTimeMillis())), expiracionInactividad, TiposCliente.clienteAdministrador, Some(nit))
    val future: Future[Validation[PersistenceException, Int]] = UsDataAdapter.asociarTokenUsuarioEmpresarialAdmin(usuario.id, token)
    future.map(_.leftMap(pe => ErrorPersistencia(pe.message, pe)).flatMap { _ =>
      Validation.success(token)
    })
  }

  /**
   * Valida si la empresa tiene alguna ip guardada
   * @param idEmpresa el id de la empresa a validar
   * @param ipPeticion ip a buscar
   * @return Future[Validation[ErrorAutenticacion, Boolean] ]
   * Success => El token si el usuario tiene la ip en su lista de ips
   * ErrorAutenticacion => ErrorPersistencia | ErrorControlIpsDesactivado
   */
  def validarControlIpsUsuarioEmpresarial(idEmpresa: Int, ipPeticion: String, token: String): Future[Validation[ErrorAutenticacion, String]] = {
    log.info("Validando control de ips usuario empresarial")
    val future = UsDataAdapter.obtenerIpsEmpresa(idEmpresa)
    future.map(_.leftMap(pe => ErrorPersistencia(pe.message, pe)).flatMap { ips =>
      if (ips.filter(_.ip == ipPeticion).isEmpty) Validation.failure(ErrorControlIpsDesactivado(token))
      else Validation.success(token)
    })
  }

  /**
   * Actualiza los ingresos erroneos de un usuario empresarial admin al numero especificado por parametro
   * @param idUsuario Id del usuario
   * @param numIngresosErroneos Numero de ingresos erroneos
   * @return Future[Validation[ErrorAutenticacion, Boolean] ]
   * Success => true
   * ErrorAutenticacion => ErrorPersistencia
   */
  def actualizarIngresosErroneosUsuarioEmpresarialAdmin(idUsuario:Int, numIngresosErroneos: Int): Future[Validation[ErrorAutenticacion, Boolean]] = {
    val future = UsDataAdapter.actualizarNumeroIngresosErroneosUsuarioEmpresarialAdmin(idUsuario, numIngresosErroneos)
    future.map(_.leftMap(pe => ErrorPersistencia(pe.message, pe)).flatMap { _ =>
      Validation.success(true)
    })
  }

  /**
   * Bloquea el usuario si se incumple la regla por parametro
   * @param idUsuario ID del usuario
   * @param numIngresosErroneos Numero de ingresos erroneos
   * @param regla Regla a validar
   * @return Future[Validation[ErrorAutenticacion, Unit] ]
   * Success => False si no se debe bloquear al usuario
   * ErrorAutenticacion => ErrorPersistencia (si algo falla) | ErrorIntentosIngresoInvalidos (si se bloqueo el usuario)
   */
  def bloquearUsuarioEmpresarialAdmin(idUsuario: Int, numIngresosErroneos: Int, regla: ReglasContrasenas): Future[Validation[ErrorAutenticacion, Boolean]] = {
    if( numIngresosErroneos + 1 == regla.valor.toInt) {
      val future = UsDataAdapter.actualizarEstadoUsuarioEmpresarialAdmin(idUsuario, EstadosEmpresaEnum.bloqueContraseña.id)
      future.map(_.leftMap(pe => ErrorPersistencia(pe.message, pe)).flatMap { _ =>
        Validation.failure(ErrorIntentosIngresoInvalidos())
      })
    }
    else Future.successful(Validation.success(false))
  }

  //----------------
  // VALIDACIONES AUTENTICACIÓN AGENTE
  //----------------

  /**
   * Se obtiene el usuario por identificacion
   * @param nit Nit del usuario
   * @param usuario Nombre de usuario
   * @return Future[Validation[ErrorAutenticacion, UsuarioEmpresarial] ]
   * Success => UsuarioEmpresarial
   * ErrorAutenticacion => ErrorPersistencia || ErrorCredencialesInvalidas
   */
  def obtenerUsuarioEmpresarialAgente(nit: String, usuario: String): Future[Validation[ErrorAutenticacion, UsuarioEmpresarial]] = {
    log.info("Obteniendo usuario empresarial agente")
    val future: Future[Validation[PersistenceException, Option[UsuarioEmpresarial]]] = UsDataAdapter.obtieneUsuarioEmpresarialPorNitYUsuario(nit, usuario)
    future.map(_.leftMap(pe => ErrorPersistencia(pe.message, pe)).flatMap {
      case Some(usuarioEmpresarial) => Validation.success(usuarioEmpresarial)
      case None => Validation.failure(ErrorCredencialesInvalidas())
    })
  }

  /**
   * Actualiza la informacion de inicio de sesion del usuario
   * @param idUsuario Id del usuario
   * @param ipPeticion Ip de la peticion
   * @return Future[Validation[ErrorAutenticacion, Boolean] ]
   * Success => True
   * ErrorAutenticacion => ErrorPersistencia
   */
  def actualizarInformacionUsuarioEmpresarialAgente(idUsuario: Int, ipPeticion: String): Future[Validation[ErrorAutenticacion, Boolean]] = {
    log.info("Actualizando informacion usuario empresarial agente")
    (for {
      ingErroneos <- ValidationT(UsDataAdapter.actualizarNumeroIngresosErroneosUsuarioEmpresarialAgente(idUsuario, 0))
      ipUltimoIngreso <- ValidationT(UsDataAdapter.actualizarIpUltimoIngresoUsuarioEmpresarialAgente(idUsuario, ipPeticion))
      fechaUltimoIngreso <- ValidationT(UsDataAdapter.actualizarFechaUltimoIngresoUsuarioEmpresarialAgente(idUsuario, new Timestamp((new Date).getTime)))
    } yield {
      true
    }).run.map(_.leftMap(pe => ErrorPersistencia(pe.message, pe)))
  }

  /**
   * Genera un token y lo asocia al usuario
   * @param usuario Usuario autenticado
   * @return Future[Validation[ErrorAutenticacion, String] ]
   * Success => Token generado y asociado
   * ErrorAutenticacion => ErrorPersistencia
   */
  def generarYAsociarTokenUsuarioEmpresarialAgente(usuario: UsuarioEmpresarial, nit: String, expiracionInactividad: String): Future[Validation[ErrorAutenticacion, String]] = {
    log.info("Generando y asociando token usuario empresarial agente")
    val token: String = Token.generarToken(usuario.nombreUsuario.get, usuario.correo, getTipoPersona(usuario.tipoIdentificacion), usuario.ipUltimoIngreso.getOrElse(""), usuario.fechaUltimoIngreso.getOrElse(new Date(System.currentTimeMillis())), expiracionInactividad, TiposCliente.agenteEmpresarial, Some(nit))
    val future: Future[Validation[PersistenceException, Int]] = UsDataAdapter.asociarTokenUsuarioEmpresarial(usuario.id, token)
    future.map(_.leftMap(pe => ErrorPersistencia(pe.message, pe)).flatMap { _ =>
      Validation.success(token)
    })
  }

  /**
   * Actualiza los ingresos erroneos de un usuario empresarial agente al numero especificado por parametro
   * @param idUsuario Id del usuario
   * @param numIngresosErroneos Numero de ingresos erroneos
   * @return Future[Validation[ErrorAutenticacion, Boolean] ]
   * Success => true
   * ErrorAutenticacion => ErrorPersistencia
   */
  def actualizarIngresosErroneosUsuarioEmpresarialAgente(idUsuario:Int, numIngresosErroneos: Int): Future[Validation[ErrorAutenticacion, Boolean]] = {
    val future = UsDataAdapter.actualizarNumeroIngresosErroneosUsuarioEmpresarialAgente(idUsuario, numIngresosErroneos)
    future.map(_.leftMap(pe => ErrorPersistencia(pe.message, pe)).flatMap { _ =>
      Validation.success(true)
    })
  }

  /**
   * Bloquea el usuario si se incumple la regla por parametro
   * @param idUsuario ID del usuario
   * @param numIngresosErroneos Numero de ingresos erroneos
   * @param regla Regla a validar
   * @return Future[Validation[ErrorAutenticacion, Unit] ]
   * Success => False si no se debe bloquear al usuario
   * ErrorAutenticacion => ErrorPersistencia (si algo falla) | ErrorIntentosIngresoInvalidos (si se bloqueo el usuario)
   */
  def bloquearUsuarioEmpresarialAgente(idUsuario: Int, numIngresosErroneos: Int, regla: ReglasContrasenas): Future[Validation[ErrorAutenticacion, Boolean]] = {
    if( numIngresosErroneos + 1 == regla.valor.toInt) {
      val future = UsDataAdapter.actualizarEstadoUsuarioEmpresarialAgente(idUsuario, EstadosEmpresaEnum.bloqueContraseña.id)
      future.map(_.leftMap(pe => ErrorPersistencia(pe.message, pe)).flatMap { _ =>
        Validation.failure(ErrorIntentosIngresoInvalidos())
      })
    }
    else Future.successful(Validation.success(false))
  }

  /**
   * Valida el estado de la empresa luego de ir a consultarlo a la base de datos por el nit asociado
   * @param nit Nit asociado al cliente administrador
   * @return Future [ Validation [ ErrorAutenticacion, Boolean] ]
   * Success => True
   * ErrorAutenticacion => ErrorEmpresaBloqueada
   * */
  def validarEstadoEmpresa(nit: String): Future[Validation[ErrorAutenticacion, Boolean]] = {
    log.info("Validando estado de la empresa")
    val empresaActiva : Int = EstadosDeEmpresaEnum.activa.id
    val estadoEmpresaFuture: Future[Validation[PersistenceException, Option[Empresa]]] = UsDataAdapter.obtenerEstadoEmpresa(nit)
    estadoEmpresaFuture.map(_.leftMap(pe => ErrorPersistencia(pe.message, pe)).flatMap {
      case Some(empresa) =>
        empresa.estado match {
          case empresaActiva => Validation.success(true)
          case _ => Validation.failure(ErrorEmpresaAccesoDenegado())
        }
      case None => Validation.failure(ErrorClienteNoExisteCore())
    })
  }

  /**
   * Obtiene el horario
   * @param idEmpresa
   * @return
   */
  def obtenerHorarioEmpresa(idEmpresa: Int): Future[Validation[ErrorAutenticacion, Option[HorarioEmpresa]]] ={
    log.info("Obteniendo horario empresa")
    val future : Future[Validation[PersistenceException, Option[HorarioEmpresa]]] = UsDataAdapter.obtenerHorarioEmpresa(idEmpresa)
    future.map(
      _.leftMap(pe => ErrorPersistencia(pe.message, pe)).flatMap{
        case Some(horarioEmpresa) => Validation.success(Some(horarioEmpresa))
        case None => Validation.success(None)
      }
    )
  }

  def validarHorarioEmpresa(horarioEmpresa: Option[HorarioEmpresa]): Future[Validation[ErrorAutenticacion, Boolean]] =  {
    log.info("Validando el horario")
    def calendarToTime(c: Calendar): Time = {
      Time.valueOf(c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE) + ":" + c.get(Calendar.SECOND))
    }
    def calendarToDate(c: Calendar): java.sql.Date = {
      java.sql.Date.valueOf(c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.DAY_OF_MONTH))
    }
    horarioEmpresa match {
      case None => Future.successful(Validation.success(true))
      case Some(horario) => {
        //Obtener la hora actual
        val calendar = Calendar.getInstance()
        val horaActual = calendarToTime(calendar)
        //1. Validar si es domingo
        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
          Future.successful(Validation.failure(ErrorHorarioIngresoEmpresa()))
        //2. Si esta habilitado el sábado, validar
        else if (!horario.sabado && calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)
          Future.successful(Validation.failure(ErrorHorarioIngresoEmpresa()))
        //3. Validar la hora de inicio y de fin
        else if (horario.horaInicio.after(horaActual) || horario.horaFin.before(horaActual))
          Future.successful(Validation.failure(ErrorHorarioIngresoEmpresa()))
        //5. Validar el día hábil
        else if (horario.diaHabil) {
          log.info("Validacioin dia habil")
          log.info("Fecha " + calendarToDate(calendar))
          UsDataAdapter.existeDiaFestivo(calendarToDate(calendar)).map(
            _.leftMap(pe => ErrorPersistencia(pe.message, pe)).flatMap {
              case true => Validation.failure(ErrorHorarioIngresoEmpresa())
              case _ => Validation.success(true)
            }
          )
        }
        else
          Future.successful(Validation.success(true))
      }
    }

  }

  /**
   * Valida el estado del usuario
   * @param estadoUsuario El estado del usuario a validar
   * @return Future[Validation[ErrorAutenticacion, Boolean] ]
   * Success => True
   * ErrorAutenticacion => ErrorUsuarioBloqueadoIntentosErroneos || ErrorUsuarioBloqueadoPendienteActivacion || ErrorUsuarioBloqueadoPendienteReinicio
   */
  override def validarEstadosUsuario(estadoUsuario: Int): Future[Validation[ErrorAutenticacion, Boolean]] = Future {
    log.info("Validando estados usuario")
    if (estadoUsuario == EstadosEmpresaEnum.bloqueContraseña.id) Validation.failure(ErrorUsuarioBloqueadoIntentosErroneos())
    else if (estadoUsuario == EstadosEmpresaEnum.pendienteActivacion.id) Validation.failure(ErrorUsuarioBloqueadoPendienteActivacion())
    else if (estadoUsuario == EstadosEmpresaEnum.pendienteReiniciarContrasena.id) Validation.failure(ErrorUsuarioBloqueadoPendienteReinicio())
    else if (estadoUsuario == EstadosEmpresaEnum.bloqueadoPorSuperAdmin.id) Validation.failure(ErrorUsuarioDesactivadoSuperAdmin())
    else Validation.success(true)
  }

    /**
    * Crea la sesion del usuario en el cluster
    * @param token Token para crear la sesion
    * @return Future[Validation[ErrorAutenticacion, Boolean] ]
    * Success => True
    * ErrorAutenticacion => ErrorPersistencia
    */
    def crearSesion(token: String, expiracionInactividad: Int, empresa: Empresa): Future[Validation[ErrorAutenticacion, Boolean]] = {
       log.info("Creando sesion")
       MainActors.sesionActorSupervisor ! CrearSesionUsuario(token, expiracionInactividad, Some(empresa))
       Future.successful(Validation.success(true))
    }

}