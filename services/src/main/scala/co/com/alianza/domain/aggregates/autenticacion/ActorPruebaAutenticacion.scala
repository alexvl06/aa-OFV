package co.com.alianza.domain.aggregates.autenticacion

import java.sql.Timestamp
import java.util.Date

import akka.actor.{ActorLogging, ActorRef, Stash, Actor}

import co.com.alianza.app.MainActors
import co.com.alianza.constants.TiposConfiguracion
import co.com.alianza.exceptions.{PersistenceException, TechnicalLevel, AlianzaException}
import co.com.alianza.infrastructure.dto.{Cliente, Usuario}
import co.com.alianza.infrastructure.messages._
import co.com.alianza.infrastructure.anticorruption.usuarios.{DataAccessAdapter => UsDataAdapter}
import co.com.alianza.infrastructure.anticorruption.clientes.{DataAccessAdapter => ClDataAdapter}
import co.com.alianza.infrastructure.anticorruption.contrasenas.{DataAccessAdapter => RgDataAdapter}
import co.com.alianza.infrastructure.anticorruption.configuraciones.{DataAccessAdapter => ConfDataAdapter}
import co.com.alianza.persistence.messages.ConsultaClienteRequest
import co.com.alianza.util.clave.Crypto
import co.com.alianza.util.json.JsonUtil
import co.com.alianza.util.token.Token
import co.com.alianza.util.transformers.ValidationT

import enumerations.{EstadosCliente, TipoIdentificacion, AppendPasswordUser, EstadosUsuarioEnum}
import org.joda.time.DateTime

import scalaz.std.AllInstances._
import spray.http.StatusCodes._

class ActorPruebaAutenticacion extends Actor with ActorLogging with Stash {

  import context.dispatcher

  def receive = {

    // External message
    case message: AutenticarMessage =>
      context.become(busy(message, sender()))
      self ! ObtenerUsuario(message.numeroIdentificacion)
  }

  def busy(message: AutenticarMessage, originalSender: ActorRef): Receive = {

    // 1. Se obtiene el usuario dado su identificacion
    case ObtenerUsuario(identificacion) =>
      log.debug("Validando usuario")
      UsDataAdapter.obtenerUsuarioNumeroIdentificacion(identificacion).map(_.map {
        case Some(usuario) => self ! ValidarEstadosUsuario(usuario)
        case None => self ! errorUsuarioCredencialesInvalidas
      }.leftMap(self ! _)).onFailure {case _ => self ! _}

    // 2. Se valida el estado del usuario encontrado
    case ValidarEstadosUsuario(usuario: Usuario) =>
      log.debug("Validando estados de usuario")
      if (usuario.estado == EstadosUsuarioEnum.bloqueContraseña.id) self ! errorUsuarioBloqueadoIntentosErroneos
      else if (usuario.estado == EstadosUsuarioEnum.pendienteActivacion.id) self ! errorUsuarioBloqueadoPendienteActivacion
      else if (usuario.estado == EstadosUsuarioEnum.pendienteReinicio.id) self ! errorUsuarioBloqueadoPendienteReinicio
      else self ! ValidarPasswords(message.password, usuario)

    // 3. Se valida que los passwords concuerden
    case ValidarPasswords(reqPw, usuario) =>
      log.debug("Validando passwords")
      val hash = Crypto.hashSha512(reqPw.concat(AppendPasswordUser.appendUsuariosFiducia))
      if (hash.contentEquals(usuario.contrasena.getOrElse(""))) self ! ObtenerClienteSP(usuario)
      else self ! PasswordInvalidoUsuario(usuario)

    // 4. Ejecuta las operaciones de password invalido
    case PasswordInvalidoUsuario(usuario) =>
      log.debug("Ejecutando excepcion password invalido")
      (for {
        actualizacion <- ValidationT(UsDataAdapter.actualizarNumeroIngresosErroneos(usuario.identificacion, usuario.numeroIngresosErroneos + 1))
        reglaOp <- ValidationT(RgDataAdapter.obtenerRegla("CANTIDAD_REINTENTOS_INGRESO_CONTRASENA"))
      } yield {
        reglaOp match {
          case None => self ! AlianzaException(new Exception("Error al obtener clave de intentos erroneos al sistema"), TechnicalLevel, "Error al obtener clave de intentos erroneos al sistema")
          case Some(regla) =>
            if (regla.valor.toInt == usuario.numeroIngresosErroneos + 1) self ! BloquearUsuario(usuario)
            else self ! errorUsuarioCredencialesInvalidas
        }
      }).run.map(_.leftMap(self ! _)).onFailure {case _ => self ! _}

    // 5. Bloquea el usuario
    case BloquearUsuario(usuario) =>
      log.debug("Bloqueando usuario")
      UsDataAdapter.actualizarEstadoUsuario(usuario.identificacion, EstadosUsuarioEnum.bloqueContraseña.id).map(_.map { resultado =>
        self ! errorIntentosIngresosInvalidos
      }.leftMap(self ! _)).onFailure {case _ => self ! _}

    // 6. Se valida el usuario en el store procedure de alianza
    case ObtenerClienteSP(usuario: Usuario) =>
      log.debug("Validando que el cliente exista en el sp de alianza")
      ClDataAdapter.consultarCliente(ConsultaClienteRequest(usuario.tipoIdentificacion, usuario.identificacion)).map(_.map {
        case Some(cliente) => self ! ValidarClienteSP(cliente, usuario)
        case None => self ! errorClienteNoExisteSP
      }.leftMap(self ! _)).onFailure {case _ => self ! _}

    // 7. Se valida el cliente que retornó el store procedure
    case ValidarClienteSP(cliente, usuario) =>
      log.debug("Validando los estados del cliente del sp")
      if (getTipoPersona(message.tipoIdentificacion) != cliente.wcli_person) self ! errorClienteNoExisteSP
      else if (cliente.wcli_estado == EstadosCliente.bloqueoContraseña) self ! errorClienteInactivoSP
      else self ! ValidarFechaCaducidadPassword(cliente, usuario)

    // 8. Se valida la fecha de caducidad del password del usuario
    case ValidarFechaCaducidadPassword(cliente, usuario) =>
      log.debug("Validando fecha de caducidad del password")
      RgDataAdapter.obtenerRegla("DIAS_VALIDA").map(_.map {
        case None => self ! AlianzaException(new Exception("Error al obtener clave días válida"), TechnicalLevel, "Error al obtener clave días válida")
        case Some(regla) =>
          // Fecha en la cual el usuario actualizo por ultima vez el password + los dias validos del password
          val caducidad = new DateTime(usuario.fechaCaducidad.getTime).plusDays(regla.valor.toInt)
          // Fecha actual
          val ahora = new DateTime()
          if (ahora.isAfter(caducidad)) {
            val token: String = Token.generarTokenCaducidadContrasena(usuario.id.get)
            self ! errorPasswordCaducado(token)
          }
          else self ! ActualizarInformacionUsuario(cliente, usuario)
      }.leftMap(self ! _)).onFailure {case _ => self ! _}

    // 9. Actualiza informacion del usuario loggeado
    case ActualizarInformacionUsuario(cliente, usuario) =>
      log.debug("Actualizando informacion del usuario")
      (for {
        ingErroneos <- ValidationT(UsDataAdapter.actualizarNumeroIngresosErroneos(usuario.identificacion, 0))
        ipUltimoIngreso <- ValidationT(UsDataAdapter.actualizarIpUltimoIngreso(usuario.identificacion, message.clientIp.get))
        fechaUltimoIngreso <- ValidationT(UsDataAdapter.actualizarFechaUltimoIngreso(usuario.identificacion,new Timestamp((new Date).getTime)))
      } yield {
        self ! ValidarControlIpsUsuario(cliente, usuario)
      }).run.map(_.leftMap(self ! _)).onFailure {case _ => self ! _}

    // 10. Se valida el control de ips del usuario
    case ValidarControlIpsUsuario(cliente, usuario) =>
      log.debug("Validando control de ips")
      UsDataAdapter.obtenerIpsUsuario(usuario.id.get).map(_.map { ips =>
        if (ips.isEmpty) self ! GenerarYAsociarTokenConflicto(cliente,usuario)
        else self ! ObtenerIpHabitualUsuario(cliente, usuario)
      }.leftMap(self ! _)).onFailure {case _ => self ! _}

    // 11. Se obtiene la ips habituales del usuario
    case ObtenerIpHabitualUsuario(cliente, usuario) =>
      log.debug("Obteniendo ips habituales")
      UsDataAdapter.obtenerIpUsuarioValida(usuario.id.get, message.clientIp.get).map(_.map {
        case Some(ip) => self ! GenerarYAsociarToken(cliente,usuario)
        case None => self ! GenerarYAsociarTokenConflicto(cliente,usuario)
      }.leftMap(self ! _)).onFailure {case _ => self ! _}

    // 12. Se asocia el token al usuario
    // Despues de asociar el token crea la sesion y se responde con un conflicto
    case GenerarYAsociarTokenConflicto(cliente, usuario) =>
      log.debug("Generando y asociando token ips desactivadas")
      val token: String = Token.generarToken(cliente.wcli_nombre, cliente.wcli_dir_correo, cliente.wcli_person, usuario.ipUltimoIngreso.getOrElse(""), usuario.fechaUltimoIngreso.getOrElse(new Date(System.currentTimeMillis())))
      UsDataAdapter.asociarTokenUsuario(usuario.identificacion, token).map(_.map { _ =>
        self ! CrearSesion(token, ipDesactivadas = true)
      }.leftMap(self ! _)).onFailure {case _ => self ! _}

    // 13. Se asocia el token al usuario
    // Despues de asociar el token crea la sesion y se responde con un OK
    case GenerarYAsociarToken(cliente, usuario) =>
      log.debug("Creando y asociando token ips activadas")
      val token: String = Token.generarToken(cliente.wcli_nombre, cliente.wcli_dir_correo, cliente.wcli_person, usuario.ipUltimoIngreso.getOrElse(""), usuario.fechaUltimoIngreso.getOrElse(new Date(System.currentTimeMillis())))
      UsDataAdapter.asociarTokenUsuario(usuario.identificacion, token).map(_.map { _ =>
        self ! CrearSesion(token, ipDesactivadas = false)
      }.leftMap(self ! _)).onFailure {case _ => self ! _}

    // 14. Se crea el actor con la sesion del usuario
    case CrearSesion(token, ipsDesactivadas) =>
      log.debug("Creando sesion")
      ConfDataAdapter.obtenerConfiguracionPorLlave(TiposConfiguracion.EXPIRACION_SESION.llave).map(_.map { conf =>
        MainActors.sesionActorSupervisor ! CrearSesionUsuario(token, conf)
        if(ipsDesactivadas) self ! AuthResponse(token, Some(controlIpsDesactivado(token)))
        else self ! AuthResponse(token, None)
      }.leftMap(self ! _)).onFailure {case _ => self ! _}

    // 15. Se terminan todas las validaciones
    case AuthResponse(token, None) =>
      originalSender ! token
      self ! RestablecerEstado()

    case AuthResponse(_, Some(error)) =>
      originalSender ! ResponseMessage(Unauthorized, JsonUtil.toJson(error))
      self ! RestablecerEstado()

    // 16. Alguna validacion le responde con un error de negocio definido abajo
    case error: ErrorMessage =>
      originalSender ! ResponseMessage(Unauthorized, JsonUtil.toJson(error))
      self ! RestablecerEstado()

    // 17. Alguna validacion le responde con una excepcion de persistencia (leftmap)
    case error: PersistenceException =>
      originalSender ! error
      self ! RestablecerEstado()

    // 18. Alguna validacion le responde con una excepcion AlianzaException
    case error: AlianzaException =>
      originalSender ! error
      self ! RestablecerEstado()

    // 19. Aliguna validacion le devuelve con otro error
    case error: Throwable =>
      originalSender ! error
      self ! RestablecerEstado()

    // 20, Restablece el estado del actor
    case RestablecerEstado() =>
      unstashAll()
      context.unbecome()

    // 21, Cualquier otro mensage que le llegue lo guarda en el stash
    case _ => stash()

  }

  protected def getTipoPersona(idTipoIdent: Int): String = {
    idTipoIdent match {
      case TipoIdentificacion.FID.identificador => "F"
      case TipoIdentificacion.NIT.identificador => "J"
      case _ => "N"
    }
  }

  // Internal messages
  case class ObtenerUsuario(identificacion: String)
  case class ValidarEstadosUsuario(usuario: Usuario)
  case class ValidarPasswords(reqPassword: String, usuario: Usuario)
  case class ObtenerClienteSP(usuario: Usuario)
  case class ValidarClienteSP(cliente: Cliente, usuario: Usuario)
  case class ValidarFechaCaducidadPassword(cliente: Cliente, usuario: Usuario)
  case class ActualizarInformacionUsuario(cliente: Cliente, usuario: Usuario)
  case class ValidarControlIpsUsuario(cliente: Cliente, usuario: Usuario)
  case class ObtenerIpHabitualUsuario(cliente: Cliente, usuario: Usuario)
  case class GenerarYAsociarTokenConflicto(cliente: Cliente, usuario:Usuario)
  case class GenerarYAsociarToken(cliente: Cliente, usuario:Usuario)
  case class CrearSesion(token: String, ipDesactivadas: Boolean)

  case class PasswordInvalidoUsuario(usuario: Usuario)
  case class BloquearUsuario(usuario: Usuario)

  case class AuthResponse(token: String, error: Option[ErrorMessage])
  case class RestablecerEstado()

  // Error messages
  val errorClienteInactivoSP = ErrorMessage("401.1", "Error Cliente Alianza", "Cliente inactivo en core de alianza")
  val errorClienteNoExisteSP = ErrorMessage("401.2", "Error Cliente Alianza", "No existe el cliente en el core de alianza")
  val errorUsuarioCredencialesInvalidas = ErrorMessage("401.3", "Error Credenciales", "Credenciales invalidas para acceder al portal de alianza fiduciaria")
  val errorIntentosIngresosInvalidos = ErrorMessage("401.7", "Usuario Bloqueado", "Ha excedido el numero máximo intentos permitidos al sistema, su usuario ha sido bloqueado")
  val errorUsuarioBloqueadoIntentosErroneos = ErrorMessage("401.8", "Usuario Bloqueado", "El usuario se encuentra bloqueado")
  val errorUsuarioBloqueadoPendienteActivacion = ErrorMessage("401.10", "Usuario Bloqueado", "El usuario se encuentra pendiente de activación")
  val errorUsuarioBloqueadoPendienteConfronta = ErrorMessage("401.11", "Usuario Bloqueado", "El usuario se encuentra bloqueado pendiente preguntas de seguridad")
  val errorUsuarioBloqueadoPendienteReinicio = ErrorMessage("401.12", "Usuario Bloqueado", "El usuario se encuentra bloqueado pendiente de reiniciar contraseña")

  def errorPasswordCaducado(token: String) = ErrorMessage("401.9", "Error Credenciales", "La contraseña del usuario ha caducado", token)
  def controlIpsDesactivado(token: String) = ErrorMessage("401.4", "Control IP", "El usuario no tiene activo el control de direcciones ip", token)
}
