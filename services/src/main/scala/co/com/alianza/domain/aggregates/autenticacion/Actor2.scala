package co.com.alianza.domain.aggregates.autenticacion

import akka.actor.{ActorLogging, Actor}

import co.com.alianza.app.MainActors
import co.com.alianza.constants.TiposConfiguracion

import co.com.alianza.domain.aggregates.autenticacion.errores._
import co.com.alianza.exceptions.PersistenceException

import co.com.alianza.infrastructure.anticorruption.usuarios.{DataAccessAdapter => UsDataAdapter}
import co.com.alianza.infrastructure.anticorruption.clientes.{DataAccessAdapter => ClDataAdapter}
import co.com.alianza.infrastructure.anticorruption.contrasenas.{DataAccessAdapter => RgDataAdapter}
import co.com.alianza.infrastructure.anticorruption.configuraciones.{DataAccessAdapter => ConfDataAdapter}
import co.com.alianza.infrastructure.dto.{Configuracion, Cliente, Usuario}
import co.com.alianza.infrastructure.messages.{AgregarIPHabitualUsuario, ResponseMessage, AutenticarMessage, CrearSesionUsuario}
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

class Actor2 extends Actor with ActorLogging {

  import context.dispatcher

  def receive = {

    /**
     * Flujo:
     * 1) Busca el usuario en la base de datos, si no se encuentra se devuelve CredencialesInvalidas
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
    case message: AutenticarMessage =>
      val originalSender = sender()

      def validaciones: Future[Validation[ErrorAutenticacion, String]] = (for {
        usuario <- ValidationT(obtenerUsuario(message.numeroIdentificacion))
        estadoValido <- ValidationT(validarEstadosUsuario(usuario.estado))
        passwordValido <- ValidationT(validarPasswords(message.password, usuario.contrasena.getOrElse("")))
        cliente <- ValidationT(obtenerClienteSP(usuario.tipoIdentificacion, usuario.identificacion))
        cienteValido <- ValidationT(validarClienteSP(usuario.tipoIdentificacion, cliente))
        passwordCaduco <- ValidationT(validarCaducidadPassword(usuario))
        actualizacionInfo <- ValidationT(actualizarInformacionUsuario(usuario.identificacion, message.clientIp.get))
        token <- ValidationT(generarYAsociarToken(cliente, usuario))
        sesion <- ValidationT(crearSesion(token))
        validacionIps <- ValidationT(validarControlIpsUsuario(usuario.id.get, message.clientIp.get, token))
      } yield validacionIps).run

      validaciones.onComplete {
        case sFailure(ex) => originalSender ! ex
        case sSuccess(resp) =>
          resp match {
            case zFailure(errorValidacion) => originalSender ! ResponseMessage(Unauthorized, errorValidacion.msg)
            case zSuccess(token) => originalSender ! token
          }
      }

    case message: AgregarIPHabitualUsuario =>
      val originalSender = sender()

      val resultadoIp: Future[Validation[ErrorAutenticacion, Boolean]] = (for {
        usuario <- ValidationT(obtenerUsuario(message.idUsuario.get))
        cliente <- ValidationT(obtenerClienteSP(usuario.tipoIdentificacion, usuario.identificacion))
        cienteValido <- ValidationT(validarClienteSP(usuario.tipoIdentificacion, cliente))
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
   * @return Future[Validation[ErrorAutenticacion, Usuario]]
   * Success => Usuario
   * ErrorAutenticacion => ErrorPersistencia || ErrorCredencialesInvalidas
   */
  def obtenerUsuario(idUsuario: Int): Future[Validation[ErrorAutenticacion, Usuario]] = {
    log.info("Validando usuario")
    val future: Future[Validation[PersistenceException, Option[Usuario]]] = UsDataAdapter.obtenerUsuarioId(idUsuario)
    future.map(_.leftMap(pe => ErrorPersistencia(pe.message, pe)).flatMap {
      case Some(usuario) => Validation.success(usuario)
      case None => Validation.failure(ErrorCredencialesInvalidas())
    })
  }

  def asociarIpUsuario(idUsuario: Int, ipPeticion: String): Future[Validation[ErrorAutenticacion, Boolean]] = {
    log.info("Validando usuario")
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
   * @return Future[Validation[ErrorAutenticacion, Usuario]]
   * Success => Usuario
   * ErrorAutenticacion => ErrorPersistencia || ErrorCredencialesInvalidas
   */
  def obtenerUsuario(identificacionUsuario: String): Future[Validation[ErrorAutenticacion, Usuario]] = {
    log.info("Validando usuario")
    val future: Future[Validation[PersistenceException, Option[Usuario]]] = UsDataAdapter.obtenerUsuarioNumeroIdentificacion(identificacionUsuario)
    future.map(_.leftMap(pe => ErrorPersistencia(pe.message, pe)).flatMap {
      case Some(usuario) => Validation.success(usuario)
      case None => Validation.failure(ErrorCredencialesInvalidas())
    })
  }

  /**
   * Valida el estado del usuario
   * @param estadoUsuario El estado del usuario a validar
   * @return Future[Validation[ErrorAutenticacion, Boolean]]
   * Success => True
   * ErrorAutenticacion => ErrorUsuarioBloqueadoIntentosErroneos || ErrorUsuarioBloqueadoPendienteActivacion || ErrorUsuarioBloqueadoPendienteReinicio
   */
  def validarEstadosUsuario(estadoUsuario: Int): Future[Validation[ErrorAutenticacion, Boolean]] = Future {
    log.info("Validando estados de usuario")
    if (estadoUsuario == EstadosUsuarioEnum.bloqueContraseña.id) Validation.failure(ErrorUsuarioBloqueadoIntentosErroneos())
    else if (estadoUsuario == EstadosUsuarioEnum.pendienteActivacion.id) Validation.failure(ErrorUsuarioBloqueadoPendienteActivacion())
    else if (estadoUsuario == EstadosUsuarioEnum.pendienteReinicio.id) Validation.failure(ErrorUsuarioBloqueadoPendienteReinicio())
    else Validation.success(true)
  }

  /**
   * Valida que los passwords concuerden
   * @param passwordPeticion Password de la peticion
   * @param passwordUsuario Password del usuario en BD
   * @return  Future[Validation[ErrorAutenticacion, Boolean]]
   * Success => True
   * ErrorAutenticacion => ErrorPasswordInvalido
   */
  def validarPasswords(passwordPeticion: String, passwordUsuario: String): Future[Validation[ErrorAutenticacion, Boolean]] = Future {
    log.info("Validando passwords")
    val hash = Crypto.hashSha512(passwordPeticion.concat(AppendPasswordUser.appendUsuariosFiducia))
    if (hash.contentEquals(passwordUsuario)) Validation.success(true)
    else Validation.failure(ErrorPasswordInvalido())
  }

  /**
   * Valida que el usuario exista en el core de alianza
   * @param tipoIdentificacionUsuario tipo de identificacion del usuario
   * @param identificacionUsuario numero de identificacion del usuario
   * @return Future[Validation[ErrorAutenticacion, Cliente]]
   * Success => Cliente
   * ErrorAutenticacion => ErrorPersistencia | ErrorClienteNoExisteCore
   */
  def obtenerClienteSP(tipoIdentificacionUsuario: Int, identificacionUsuario: String): Future[Validation[ErrorAutenticacion, Cliente]] = {
    log.info("Validando que el cliente exista en el core de alianza")
    val future: Future[Validation[PersistenceException, Option[Cliente]]] = ClDataAdapter.consultarCliente(ConsultaClienteRequest(tipoIdentificacionUsuario, identificacionUsuario))
    future.map(_.leftMap(pe => ErrorPersistencia(pe.message, pe)).flatMap {
      case Some(cliente) => Validation.success(cliente)
      case None => Validation.failure(ErrorClienteNoExisteCore())
    })
  }

  /**
   * Valida los estados del usuario del core de alianza
   * @param tipoIdentificacionUsuario tipo de identificacion del usuario
   * @param cliente cliente a validar
   * @return Future[Validation[ErrorAutenticacion, Boolean]]
   * Success => True
   * ErrorAutenticacion => ErrorClienteNoExisteCore | ErrorClienteInactivoCore
   */
  def validarClienteSP(tipoIdentificacionUsuario: Int, cliente: Cliente): Future[Validation[ErrorAutenticacion, Boolean]] = Future {
    log.info("Validando los estados del cliente del core")
    if (getTipoPersona(tipoIdentificacionUsuario) != cliente.wcli_person) Validation.failure(ErrorClienteNoExisteCore())
    else if (cliente.wcli_estado == EstadosCliente.bloqueoContraseña) Validation.failure(ErrorClienteInactivoCore())
    else Validation.success(true)
  }

  /**
   * Valida la fecha de caducidad de la contraseña de un usuario
   * @param usuario Usuario a validar la contraseña
   * @return Future[Validation[ErrorAutenticacion, Boolean]]
   * Success => True
   * ErrorAutenticacion => ErrorPersistencia | ErrorRegla | ErrorPasswordCaducado
   */
  def validarCaducidadPassword(usuario: Usuario): Future[Validation[ErrorAutenticacion, Boolean]] = {
    log.info("Validando fecha de caducidad del password")
    val future: Future[Validation[PersistenceException, Option[ReglasContrasenas]]] = RgDataAdapter.obtenerRegla("DIAS_VALIDA")
    future.map(_.leftMap(pe => ErrorPersistencia(pe.message, pe)).flatMap {
      case None => Validation.failure(ErrorRegla("DIAS_VALIDA"))
      case Some(regla) =>
        if (new DateTime().isAfter(new DateTime(usuario.fechaCaducidad.getTime).plusDays(regla.valor.toInt))) {
          val token: String = Token.generarTokenCaducidadContrasena(usuario.id.get)
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
   * @return Future[Validation[ErrorAutenticacion, Boolean]]
   * Success => True
   * ErrorAutenticacion => ErrorPersistencia
   */
  def actualizarInformacionUsuario(identificacionUsuario: String, ipPeticion: String): Future[Validation[ErrorAutenticacion, Boolean]] = {
    log.info("Actualizando informacion del usuario")
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
   * @return Future[Validation[ErrorAutenticacion, String]]
   * Success => Token generado y asociado
   * ErrorAutenticacion => ErrorPersistencia
   */
  def generarYAsociarToken(cliente: Cliente, usuario: Usuario): Future[Validation[ErrorAutenticacion, String]] = {
    log.info("Generando y asociando token")
    val token: String = Token.generarToken(cliente.wcli_nombre, cliente.wcli_dir_correo, cliente.wcli_person, usuario.ipUltimoIngreso.getOrElse(""), usuario.fechaUltimoIngreso.getOrElse(new Date(System.currentTimeMillis())))
    val future: Future[Validation[PersistenceException, Int]] = UsDataAdapter.asociarTokenUsuario(usuario.identificacion, token)
    future.map(_.leftMap(pe => ErrorPersistencia(pe.message, pe)).flatMap { _ =>
      Validation.success(token)
    })
  }

  /**
   * Crea la sesion del usuario en el cluster
   * @param token Token para crear la sesion
   * @return Future[Validation[ErrorAutenticacion, Boolean]]
   * Success => True
   * ErrorAutenticacion => ErrorPersistencia
   */
  def crearSesion(token: String): Future[Validation[ErrorAutenticacion, Boolean]] = {
    log.info("Creando sesion")
    val future: Future[Validation[PersistenceException, Option[Configuracion]]] = ConfDataAdapter.obtenerConfiguracionPorLlave(TiposConfiguracion.EXPIRACION_SESION.llave)
    future.map(_.leftMap(pe => ErrorPersistencia(pe.message, pe)).flatMap { conf =>
      MainActors.sesionActorSupervisor ! CrearSesionUsuario(token, conf)
      Validation.success(true)
    })
  }

  /**
   * Valida si el usuario tiene alguna ip guardada
   * @param idUsuario el id del usuario a validar
   * @return Future[Validation[ErrorAutenticacion, Boolean]]
   * Success => El token si el usuario tiene la ip en su lista de ips
   * ErrorAutenticacion => ErrorPersistencia | ErrorControlIpsDesactivado
   */
  def validarControlIpsUsuario(idUsuario: Int, ipPeticion: String, token: String): Future[Validation[ErrorAutenticacion, String]] = {
    log.info("Validando control de ips")
    val future = UsDataAdapter.obtenerIpsUsuario(idUsuario)
    future.map(_.leftMap(pe => ErrorPersistencia(pe.message, pe)).flatMap { ips =>
      if (ips.filter(_.ip == ipPeticion).isEmpty) Validation.failure(ErrorControlIpsDesactivado(token))
      else Validation.success(token)
    })
  }

  //----------------
  // ERRORES
  //----------------

  // Cuando el password es invalido se deben ejecutar operaciones sobre la base de datos
  def passwordInvalido(identificacionUsuario: String, numIngresosErroneosUsuario: Int) = {
    log.info("Validando passwords")
  }

  // Cuando se cumple que el usuario supera las contraseñas incorrectas maximas permitidas
  def bloquearUsuario(identificacionUsuario: String) = {
    log.info("Bloqueando usuario")
  }


  protected def getTipoPersona(idTipoIdent: Int): String = {
    idTipoIdent match {
      case TipoIdentificacion.FID.identificador => "F"
      case TipoIdentificacion.NIT.identificador => "J"
      case _ => "N"
    }
  }
}
