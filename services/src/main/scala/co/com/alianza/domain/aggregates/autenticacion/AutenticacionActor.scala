package co.com.alianza.domain.aggregates.autenticacion

import akka.actor.{Props, ActorLogging, Actor}
import akka.routing.RoundRobinPool

import co.com.alianza.app.MainActors
import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.commons.enumerations.TiposCliente.TiposCliente
import co.com.alianza.commons.enumerations.TiposCliente.TiposCliente
import co.com.alianza.constants.TiposConfiguracion

import co.com.alianza.domain.aggregates.autenticacion.errores._
import co.com.alianza.domain.aggregates.usuarios.ErrorClienteNoExiste
import co.com.alianza.exceptions.PersistenceException

import co.com.alianza.infrastructure.anticorruption.usuarios.{DataAccessAdapter => UsDataAdapter}
import co.com.alianza.infrastructure.anticorruption.clientes.{DataAccessAdapter => ClDataAdapter}
import co.com.alianza.infrastructure.anticorruption.contrasenas.{DataAccessAdapter => RgDataAdapter}
import co.com.alianza.infrastructure.anticorruption.configuraciones.{DataAccessAdapter => ConfDataAdapter}
import co.com.alianza.infrastructure.dto.{Configuracion, Cliente, Usuario}
import co.com.alianza.infrastructure.messages._
import co.com.alianza.persistence.entities.ReglasContrasenas
import co.com.alianza.persistence.messages.ConsultaClienteRequest
import co.com.alianza.util.clave.Crypto
import co.com.alianza.util.token.Token
import co.com.alianza.util.transformers.ValidationT

import enumerations.{TipoIdentificacion, EstadosCliente, AppendPasswordUser, EstadosUsuarioEnum}

import java.sql.Timestamp
import java.util.Date

import org.joda.time.DateTime
import spray.http.StatusCodes._

import scala.concurrent.Future
import scala.util.{Success => sSuccess, Failure => sFailure}

import scalaz.std.AllInstances._
import scalaz.{Failure => zFailure, Success => zSuccess, Validation}
import scalaz.Validation.FlatMap._

class AutenticacionActorSupervisor extends Actor with ActorLogging {
  import akka.actor.SupervisorStrategy._
  import akka.actor.OneForOneStrategy

  val autenticacionActor = context.actorOf(Props[AutenticacionActor].withRouter(RoundRobinPool(nrOfInstances = 5)), "autenticacionActor")
  val autenticacionUsuarioEmpresaActor = context.actorOf(Props[AutenticacionUsuarioEmpresaActor].withRouter(RoundRobinPool(nrOfInstances = 5)), "autenticacionUsuarioEmpresaActor")

  def receive = {

    case m: AutenticarUsuarioEmpresarialMessage => autenticacionUsuarioEmpresaActor forward m
    case m: AgregarIPHabitualUsuarioEmpresarialAdmin => autenticacionUsuarioEmpresaActor forward m
    case m: AgregarIPHabitualUsuarioEmpresarialAgente => autenticacionUsuarioEmpresaActor forward m

    case message: Any =>
      autenticacionActor forward message

  }

  override val supervisorStrategy = OneForOneStrategy() {
    case exception: Exception =>
      exception.printStackTrace()
      log.error(exception, exception.getMessage)
      Restart
  }

}

class AutenticacionActor extends Actor with ActorLogging {

  import context.dispatcher

  def receive = {

    /**
     * Flujo:
     * 1) Busca el usuario en la base de datos, si no se encuentra se devuelve CredencialesInvalidas
     * *) Antes validaba el tipo de identificacion. Por solicitud de Alianza ya no se hace esa validacion.
     * 2) Valida los estados del usuario encontrado, esta validacion devuelve un tipo de error por estado, si es exitosa se continúa el proceso
     * 3) Se comparan los passwords de la petición y el usuario, si coinciden se prosigue de lo contrario se debe ejecutar la excepcion de pw inválido
     * 4) Se busca el cliente en el core de alianza, si no se encuentra se debe devolver ErrorClienteNoExisteCore
     * 5) Se valida el cliente encontrado, este metodo devuelve un error de la validacion que no cumple
     * 6) Se valida la fecha de caducacion del password, si caducó se debe devolver ErrorPasswordCaducado, de lo contrario se prosigue
     * ------- Si pasan las 6 validaciones anteriores, el usuario se considera como usuario autenticado --------
     * 7) Se actualiza la información de numIngresosErroneos, ipUltimoIngreso y fechaUltimoIngreso del usuario
     * 8) Se asigna el tiempo de expiración
     * 9) Se genera un token y se asocia al usuario
     * 10) Se crea la sesion del usuario en el cluster
     * 11) Se valida si el usuario tiene alguna ip guardada, si es así se procede a validar si es una ip habitual, de lo contrario se genera un token (10), una sesion (11) y se responde con ErrorControlIpsDesactivado
     */
    case message: AutenticarMessage =>
      val originalSender = sender()
      val validaciones: Future[Validation[ErrorAutenticacion, String]] = (for {
        usuario           <- ValidationT(obtenerUsuario(message.numeroIdentificacion))
        estadoValido      <- ValidationT(validarEstadosUsuario(usuario.estado))
        passwordValido    <- ValidationT(validarPasswords(message.password, usuario.contrasena.getOrElse(""), Some(usuario.identificacion), usuario.id, usuario.numeroIngresosErroneos))
        cliente           <- ValidationT(obtenerClienteSP(usuario.identificacion))
        cienteValido      <- ValidationT(validarClienteSP(cliente))
        passwordCaduco    <- ValidationT(validarCaducidadPassword(TiposCliente.clienteIndividual, usuario.id.get, usuario.fechaCaducidad))
        actualizacionInfo <- ValidationT(actualizarInformacionUsuario(usuario.identificacion, message.clientIp.get))
        inactividadConfig <- ValidationT(buscarConfiguracion(TiposConfiguracion.EXPIRACION_SESION.llave))
        token             <- ValidationT(generarYAsociarToken(cliente, usuario, inactividadConfig.valor, message.clientIp.get))
        sesion            <- ValidationT(crearSesion(token, inactividadConfig.valor.toInt))
        validacionIps     <- ValidationT(validarControlIpsUsuario(usuario.id.get, message.clientIp.get, token))
      } yield validacionIps).run

      validaciones.onComplete {
        case sFailure(_) => originalSender ! _
        case sSuccess(resp) => resp match {
          case zSuccess(token) => originalSender ! token
          case zFailure(errorAutenticacion) => errorAutenticacion match {
            case err @ ErrorPersistencia(_, ep1) => originalSender ! ep1
            case err @ ErrorPasswordInvalido(identificacionUsuario, _, numIngresosErroneosUsuario) =>

              val ejecucion: Future[Validation[ErrorAutenticacion, Boolean]] = (for {
                ingresosErroneos <- ValidationT(actualizarIngresosErroneosUsuario(identificacionUsuario.get, numIngresosErroneosUsuario + 1))
                regla <- ValidationT(buscarRegla("CANTIDAD_REINTENTOS_INGRESO_CONTRASENA"))
                bloqueo <- ValidationT(bloquearUsuario(identificacionUsuario.get, numIngresosErroneosUsuario, regla))
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
    case message: AgregarIPHabitualUsuario =>
      val originalSender = sender()

      val resultadoIp: Future[Validation[ErrorAutenticacion, Boolean]] = (for {
        usuario <- ValidationT(obtenerUsuario(message.idUsuario.get))
        cliente <- ValidationT(obtenerClienteSP(usuario.identificacion))
        cienteValido <- ValidationT(validarClienteSP(cliente))
        relacionarIp <- ValidationT(asociarIpUsuario(message.idUsuario.get, message.clientIp.get))
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
  // AGREGAR IPS
  //----------------

  /**
   * Se obtiene el usuario por id
   * @param idUsuario Numero de identificacion del usuario
   * @return Future[Validation[ErrorAutenticacion, Usuario] ]
   * Success => Usuario
   * ErrorAutenticacion => ErrorPersistencia || ErrorCredencialesInvalidas
   */
  def obtenerUsuario(idUsuario: Int): Future[Validation[ErrorAutenticacion, Usuario]] = {
    log.info("Obteniendo usuario individual")
    val future: Future[Validation[PersistenceException, Option[Usuario]]] = UsDataAdapter.obtenerUsuarioId(idUsuario)
    future.map(_.leftMap(pe => ErrorPersistencia(pe.message, pe)).flatMap {
      case Some(usuario) => Validation.success(usuario)
      case None => Validation.failure(ErrorCredencialesInvalidas())
    })
  }

  /**
   * Asocia la ip al usuario
   * @param idUsuario id del usuario
   * @param ipPeticion ip a asociar
   * @return Future[Validation[ErrorAutenticacion, Boolean] ]
   * Success => True
   * ErrorAutenticacion => PersistenceException
   */
  def asociarIpUsuario(idUsuario: Int, ipPeticion: String): Future[Validation[ErrorAutenticacion, Boolean]] = {
    log.info("Asociando ip usuario individual")
    val future: Future[Validation[PersistenceException, String]] = UsDataAdapter.relacionarIp(idUsuario, ipPeticion)
    future.map(_.leftMap(pe => ErrorPersistencia(pe.message, pe)).flatMap { _ =>
      Validation.success(true)
    })
  }

  //----------------
  // VALIDACIONES AUTENTICACIÓN
  //----------------

  /**
   * Se obtiene el usuario por identificacion
   * @param identificacionUsuario Numero de identificacion del usuario
   * @return Future[Validation[ErrorAutenticacion, Usuario] ]
   * Success => Usuario
   * ErrorAutenticacion => ErrorPersistencia || ErrorCredencialesInvalidas
   */
  def obtenerUsuario(identificacionUsuario: String): Future[Validation[ErrorAutenticacion, Usuario]] = {
    log.info("Obteniendo usuario individual")
    val future: Future[Validation[PersistenceException, Option[Usuario]]] = UsDataAdapter.obtenerUsuarioNumeroIdentificacion(identificacionUsuario)
    future.map(_.leftMap(pe => ErrorPersistencia(pe.message, pe)).flatMap {
      case Some(usuario) => Validation.success(usuario)
      case None => Validation.failure(ErrorCredencialesInvalidas())
    })
  }

  /**
   * Valida el estado del usuario
   * @param estadoUsuario El estado del usuario a validar
   * @return Future[Validation[ErrorAutenticacion, Boolean] ]
   * Success => True
   * ErrorAutenticacion => ErrorUsuarioBloqueadoIntentosErroneos || ErrorUsuarioBloqueadoPendienteActivacion || ErrorUsuarioBloqueadoPendienteReinicio
   */
  def validarEstadosUsuario(estadoUsuario: Int): Future[Validation[ErrorAutenticacion, Boolean]] = Future {

    log.info("Validando estados usuario")
    if (estadoUsuario == EstadosUsuarioEnum.bloqueContraseña.id) Validation.failure(ErrorUsuarioBloqueadoIntentosErroneos())
    else if (estadoUsuario == EstadosUsuarioEnum.pendienteActivacion.id) Validation.failure(ErrorUsuarioBloqueadoPendienteActivacion())
    else if (estadoUsuario == EstadosUsuarioEnum.pendienteReinicio.id) Validation.failure(ErrorUsuarioBloqueadoPendienteReinicio())
    else Validation.success(true)
  }

  /**
   * Valida que los passwords concuerden
   * @param passwordPeticion Password de la peticion
   * @param passwordUsuario Password del usuario en BD
   * @return  Future[Validation[ErrorAutenticacion, Boolean] ]
   * Success => True
   * ErrorAutenticacion => ErrorPasswordInvalido
   */
  def validarPasswords(passwordPeticion: String, passwordUsuario: String, identificacionUsuario: Option[String], idUsuario: Option[Int], numIngresosErroneosUsuario: Int): Future[Validation[ErrorAutenticacion, Boolean]] = Future {
    log.info("Validando passwords")
    val hash = Crypto.hashSha512(passwordPeticion.concat(AppendPasswordUser.appendUsuariosFiducia), idUsuario.get)
    if (hash.contentEquals(passwordUsuario)) Validation.success(true)
    else Validation.failure(ErrorPasswordInvalido(identificacionUsuario, idUsuario, numIngresosErroneosUsuario))
  }

  /**
   * Valida que el usuario exista en el core de alianza
   * @param identificacionUsuario numero de identificacion del usuario
   * @return Future[Validation[ErrorAutenticacion, Cliente] ]
   * Success => Cliente
   * ErrorAutenticacion => ErrorPersistencia | ErrorClienteNoExisteCore
   */
  def obtenerClienteSP(identificacionUsuario: String): Future[Validation[ErrorAutenticacion, Cliente]] = {
    log.info("Validando que el cliente exista en el core de alianza")
    val future: Future[Validation[PersistenceException, Option[Cliente]]] = ClDataAdapter.consultarCliente(identificacionUsuario)
    future.map(_.leftMap(pe => ErrorPersistencia(pe.message, pe)).flatMap {
      case Some(cliente) => Validation.success(cliente)
      case None => Validation.failure(ErrorClienteNoExisteCore())
    })
  }

  def validacionTipoIdentificacion(message: AutenticarMessage, usuario: Usuario): Future[Validation[ErrorAutenticacion, Boolean]] = Future {
    log.info("Validando que los tipos de documentos concuerden")
    val validacionTipoIdentificacion = usuario.tipoIdentificacion == message.tipoIdentificacion
    validacionTipoIdentificacion match {
      case false => Validation.failure(ErrorCredencialesInvalidas())
      case true  => Validation.success(true)
    }
  }

  /**
   * Valida los estados del usuario del core de alianza
   * @param cliente cliente a validar
   * @return Future[Validation[ErrorAutenticacion, Boolean] ]
   * Success => True
   * ErrorAutenticacion => ErrorClienteNoExisteCore | ErrorClienteInactivoCore
   */
  def validarClienteSP(cliente: Cliente): Future[Validation[ErrorAutenticacion, Boolean]] = Future {
    log.info("Validando los estados del cliente del core")
    if (cliente.wcli_estado != EstadosCliente.inactivo && cliente.wcli_estado != EstadosCliente.bloqueado &&
      cliente.wcli_estado != EstadosCliente.activo)
      Validation.failure(ErrorClienteInactivoCore())
    else Validation.success(true)
  }

  /**
   * Valida la fecha de caducidad de la contraseña de un usuario
   * @param idUsuario Id del usuario a validar
   * @param fechaActualizacionUsuario Fecha de actualizacion del usuario
   * @return Future[Validation[ErrorAutenticacion, Boolean] ]
   * Success => True
   * ErrorAutenticacion => ErrorPersistencia | ErrorRegla | ErrorPasswordCaducado
   */
  def validarCaducidadPassword(tipoCliente : TiposCliente, idUsuario: Int, fechaActualizacionUsuario: Date): Future[Validation[ErrorAutenticacion, Boolean]] = {
    log.info("Validando fecha de caducidad del password")
    val future: Future[Validation[PersistenceException, Option[ReglasContrasenas]]] = RgDataAdapter.obtenerRegla("DIAS_VALIDA")
    future.map(_.leftMap(pe => ErrorPersistencia(pe.message, pe)).flatMap {
      case None => Validation.failure(ErrorRegla("DIAS_VALIDA"))
      case Some(regla) =>
        if (new DateTime().isAfter(new DateTime(fechaActualizacionUsuario.getTime).plusDays(regla.valor.toInt))) {
          val token: String = Token.generarTokenCaducidadContrasena(tipoCliente, idUsuario)
          Validation.failure(ErrorPasswordCaducado(token))
        }
        else {
          Validation.success(true)
        }
    })
  }

  /**
   * Actualiza la informacion de inicio de sesion del usuario
   * @param identificacionUsuario Numero de identificacion del usuario
   * @param ipPeticion Ip de la peticion
   * @return Future[Validation[ErrorAutenticacion, Boolean] ]
   * Success => True
   * ErrorAutenticacion => ErrorPersistencia
   */
  def actualizarInformacionUsuario(identificacionUsuario: String, ipPeticion: String): Future[Validation[ErrorAutenticacion, Boolean]] = {
    log.info("Actualizando informacion del usuario individual")
    (for {
      ingErroneos <- ValidationT(UsDataAdapter.actualizarNumeroIngresosErroneos(identificacionUsuario, 0))
      ipUltimoIngreso <- ValidationT(UsDataAdapter.actualizarIpUltimoIngreso(identificacionUsuario, ipPeticion))
      fechaUltimoIngreso <- ValidationT(UsDataAdapter.actualizarFechaUltimoIngreso(identificacionUsuario, new Timestamp((new Date).getTime)))
    } yield {
      true
    }).run.map(_.leftMap(pe => ErrorPersistencia(pe.message, pe)))
  }

  /**
   * Genera un token y lo asocia al usuario
   * @param cliente cliente del core de alianza
   * @param usuario Usuario autenticado
   * @return Future[Validation[ErrorAutenticacion, String] ]
   * Success => Token generado y asociado
   * ErrorAutenticacion => ErrorPersistencia
   */
  def generarYAsociarToken(cliente: Cliente, usuario: Usuario, expiracionInactividad: String, ipCliente: String): Future[Validation[ErrorAutenticacion, String]] = {
    log.info("Generando y asociando token usuario individual")
    val token: String = Token.generarToken(cliente.wcli_nombre, cliente.wcli_dir_correo, cliente.wcli_person, usuario.ipUltimoIngreso.getOrElse(ipCliente), usuario.fechaUltimoIngreso.getOrElse(new Date(System.currentTimeMillis())), expiracionInactividad)
    val future: Future[Validation[PersistenceException, Int]] = UsDataAdapter.asociarTokenUsuario(usuario.identificacion, token)
    future.map(_.leftMap(pe => ErrorPersistencia(pe.message, pe)).flatMap { _ =>
      Validation.success(token)
    })
  }

  /**
   * Crea la sesion del usuario en el cluster
   * @param token Token para crear la sesion
   * @return Future[Validation[ErrorAutenticacion, Boolean] ]
   * Success => True
   * ErrorAutenticacion => ErrorPersistencia
   */
  def crearSesion(token: String, expiracionInactividad: Int): Future[Validation[ErrorAutenticacion, Boolean]] = {
    log.info("Creando sesion")
    MainActors.sesionActorSupervisor ! CrearSesionUsuario(token, expiracionInactividad)
    Future.successful(Validation.success(true))
  }

  /**
   * Valida si el usuario tiene alguna ip guardada
   * @param idUsuario el id del usuario a validar
   * @return Future[Validation[ErrorAutenticacion, Boolean] ]
   * Success => El token si el usuario tiene la ip en su lista de ips
   * ErrorAutenticacion => ErrorPersistencia | ErrorControlIpsDesactivado
   */
  def validarControlIpsUsuario(idUsuario: Int, ipPeticion: String, token: String): Future[Validation[ErrorAutenticacion, String]] = {
    log.info("Validando control de ips usuario individual")
    val future = UsDataAdapter.obtenerIpsUsuario(idUsuario)
    future.map(_.leftMap(pe => ErrorPersistencia(pe.message, pe)).flatMap { ips =>
      if (ips.filter(_.ip == ipPeticion).isEmpty) Validation.failure(ErrorControlIpsDesactivado(token))
      else Validation.success(token)
    })
  }

  /**
   * Actualiza los ingresos erroneos de un usuario al numero especificado por parametro
   * @param identificacionUsuario Identificacion del usuario
   * @param numIngresosErroneos Numero de ingresos erroneos
   * @return Future[Validation[ErrorAutenticacion, Boolean] ]
   * Success => true
   * ErrorAutenticacion => ErrorPersistencia
   */
  def actualizarIngresosErroneosUsuario(identificacionUsuario:String, numIngresosErroneos: Int): Future[Validation[ErrorAutenticacion, Boolean]] = {
    val future = UsDataAdapter.actualizarNumeroIngresosErroneos(identificacionUsuario, numIngresosErroneos)
    future.map(_.leftMap(pe => ErrorPersistencia(pe.message, pe)).flatMap { _ =>
      Validation.success(true)
    })
  }

  /**
   * Busca una regla dado una llave por parametro
   * @param llave llave de la regla a buscar
   * @return Future[Validation[ErrorAutenticacion, ReglasContrasenas] ]
   * Success => ReglasContrasenas
   * ErrorAutenticacion => ErrorPersistencia | ErrorRegla
   */
  def buscarRegla(llave: String): Future[Validation[ErrorAutenticacion, ReglasContrasenas]] = {
    val future = RgDataAdapter.obtenerRegla(llave)
    future.map(_.leftMap(pe => ErrorPersistencia(pe.message, pe)).flatMap {
      case None => Validation.failure(ErrorRegla(llave))
      case Some(regla) => Validation.success(regla)
    })
  }

  /**
   * Busca una configuracion dado una llave por parametro
   * @param llave llave de la configuracion a buscar
   * @return Future[Validation[ErrorAutenticacion, Configuracion] ]
   * Success => Configuracion
   * ErrorAutenticacion => ErrorPersistencia | ErrorRegla
   */
  def buscarConfiguracion(llave: String): Future[Validation[ErrorAutenticacion, Configuracion]] = {
    val future = ConfDataAdapter.obtenerConfiguracionPorLlave(llave)
    future.map(_.leftMap(pe => ErrorPersistencia(pe.message, pe)).flatMap {
      case None => Validation.failure(ErrorRegla(llave))
      case Some(conf) => Validation.success(conf)
    })
  }

  /**
   * Bloquea el usuario si se incumple la regla por parametro
   * @param identificacionUsuario Identificacion del usuario
   * @param numIngresosErroneos Numero de ingresos erroneos
   * @param regla Regla a validar
   * @return Future[Validation[ErrorAutenticacion, Unit] ]
   * Success => False si no se debe bloquear al usuario
   * ErrorAutenticacion => ErrorPersistencia (si algo falla) | ErrorIntentosIngresoInvalidos (si se bloqueo el usuario)
   */
  def bloquearUsuario(identificacionUsuario: String, numIngresosErroneos: Int, regla: ReglasContrasenas): Future[Validation[ErrorAutenticacion, Boolean]] = {
    if( numIngresosErroneos + 1 == regla.valor.toInt) {
      val future = UsDataAdapter.actualizarEstadoUsuario(identificacionUsuario, EstadosUsuarioEnum.bloqueContraseña.id)
      future.map(_.leftMap(pe => ErrorPersistencia(pe.message, pe)).flatMap { _ =>
        Validation.failure(ErrorIntentosIngresoInvalidos())
      })
    }
    else Future.successful(Validation.success(false))
  }

  /**
   * Devuelve el tipo de identificacion de la persona
   * @param idTipoIdent Id del tipo de identificacion
   * @return F si es fiduciaria, J si es juridica y N si es natural
   */
  protected def getTipoPersona(idTipoIdent: Int): String = {
    idTipoIdent match {
      case TipoIdentificacion.FID.identificador => "F"
      case TipoIdentificacion.NIT.identificador => "J"
      case _ => "N"
    }
  }
}