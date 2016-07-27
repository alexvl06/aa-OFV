package portal.transaccional.autenticacion.service.drivers.autenticacion

import java.util.Date

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.constants.{ LlavesReglaContrasena, TiposConfiguracion }
import co.com.alianza.domain.aggregates.autenticacion.SesionActorSupervisor
import co.com.alianza.exceptions.ValidacionException
import co.com.alianza.infrastructure.messages.CrearSesionUsuario
import co.com.alianza.persistence.entities.{ Empresa, UsuarioEmpresarial, UsuarioEmpresarialAdmin }
import co.com.alianza.util.token.Token
import enumerations.{ EstadosEmpresaEnum, TipoIdentificacion }
import portal.transaccional.autenticacion.service.drivers.cliente.ClienteRepository
import portal.transaccional.autenticacion.service.drivers.configuracion.ConfiguracionRepository
import portal.transaccional.autenticacion.service.drivers.empresa.EmpresaRepository
import portal.transaccional.autenticacion.service.drivers.ipempresa.IpEmpresaRepository
import portal.transaccional.autenticacion.service.drivers.reglas.ReglaContrasenaRepository
import portal.transaccional.autenticacion.service.drivers.usuario.{ UsuarioEmpresarialAdminRepository, UsuarioEmpresarialRepository }

import scala.concurrent.{ ExecutionContext, Future }
import scala.reflect.ClassTag

case class AutenticacionEmpresaDriverRepository(
    usuarioRepo: UsuarioEmpresarialRepository, usuarioAdminRepo: UsuarioEmpresarialAdminRepository, clienteCoreRepo: ClienteRepository,
    empresaRepo: EmpresaRepository, reglaRepo: ReglaContrasenaRepository, configuracionRepo: ConfiguracionRepository, ipRepo: IpEmpresaRepository,
    sessionActor: ActorRef )(implicit val ex: ExecutionContext) extends AutenticacionEmpresaRepository {

  implicit val timeout = Timeout(5.seconds)

  /**
   * Flujo:
   * Se consulta si el usuario a autenticar es un agente empresarial, de ser
   * así se manda un mensaje el mismo para autenticar ese tipo de usuario
   *
   * Si no se cumple lo anterior se valida si el usuario es cliente administrador,
   * si es asi se manda un mensaje el mismo para autenticar ese tipo de usuario
   *
   * Si no se cumple ninguna de las dos cosas se retorna ErrorCredencialesInvalidas
   */
  def autenticarUsuarioEmpresa(identificacion: String, usuario: String, contrasena: String, ip: String): Future[String] = {
    for {
      esAgente <- usuarioRepo.getByIdentityAndUser(identificacion, usuario)
      esAdmin <- usuarioAdminRepo.getByIdentityAndUser(identificacion, usuario)
      autenticacion <- autenticar(esAgente, esAdmin, contrasena, ip)
    } yield autenticacion
  }

  /**
   * Autentica según el tipo de cliente (Agente, o Admin)
   * @param agente Posible agente empresarial que quiera autenticarse
   * @param admin Posible admin empresarial que quiera autenticarse
   * @param ip Ip del usuario que desea autenticarse
   * @return Future[Boolean]
   * Success => True
   */
  private def autenticar(agente: Option[UsuarioEmpresarial], admin: Option[UsuarioEmpresarialAdmin], contrasena: String, ip: String): Future[String] = {
    if (agente.isDefined) {
      autenticarAgente(agente.get, contrasena, ip)
    } else if (admin.isDefined) {
      autenticarAdministrador(admin.get, contrasena, ip)
    } else { Future.failed(ValidacionException("401.3", "Error Credenciales")) }
  }

  /**
   * Flujo:
   * - buscar y validar empresa
   * - obtener regla de reintentos
   * - validar usuario
   * - validar estado
   * - obtener cliente core
   * - validar estado cliente core
   * - obtener regla dias
   * - validar caducidad
   * - actualizar usuario
   * - obtener ips
   * - validar ips
   * - obtener configuracion inactividad
   * - generar token
   * - asociar token
   * - crear session de usuario
   */
  private def autenticarAgente(usuario: UsuarioEmpresarial, contrasena: String, ip: String): Future[String] = {
    for {
      empresa <- obtenerEmpresaValida(usuario.identificacion)
      reintentosErroneos <- reglaRepo.getRegla(LlavesReglaContrasena.DIAS_VALIDA.llave)
      validar <- usuarioRepo.validarUsuario(usuario, contrasena, reintentosErroneos.valor.toInt)
      cliente <- clienteCoreRepo.getCliente(usuario.identificacion)
      estadoCore <- clienteCoreRepo.validarEstado(cliente)
      reglaDias <- reglaRepo.getRegla(LlavesReglaContrasena.DIAS_VALIDA.llave)
      caducidad <- usuarioRepo.validarCaducidadContrasena(TiposCliente.agenteEmpresarial, usuario, reglaDias.valor.toInt)
      actualizar <- usuarioRepo.actualizarInfoUsuario(usuario, ip)
      ips <- ipRepo.getIpsByEmpresaId(empresa.id)
      validacionIps <- ipRepo.validarControlIpAgente(ip, ips)
      inactividad <- configuracionRepo.getConfiguracion(TiposConfiguracion.EXPIRACION_SESION.llave)
      token <- generarTokenAgente(usuario, ip, inactividad.llave)
      asociarToken <- usuarioRepo.actualizarToken(usuario.id, token)
      //TODO: poner la empresa (dto)
      sesion <- actorResponse[SesionActorSupervisor.SesionUsuarioCreada](sessionActor, CrearSesionUsuario(token, inactividad.valor.toInt))
    } yield token
  }

  def actorResponse[T: ClassTag](actor: ActorRef, msg: CrearSesionUsuario): Future[T] = {
    (actor ? msg).mapTo[T]
  }

  /**
   * Flujo:
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
  /**
   * Flujo:
   * - buscar y validar empresa
   * - obtener regla de reintentos
   * - validar usuario
   * - obtener cliente core
   * - validar estado cliente core
   * - obtener regla dias
   * - validar caducidad
   * - actualizar usuario
   * - obtener ips
   * - validar ips
   * - obtener configuracion inactividad
   * - generar token
   * - asociar token
   * - crear session de usuario
   */
  private def autenticarAdministrador(usuario: UsuarioEmpresarialAdmin, contrasena: String, ip: String): Future[String] = {
    for{
      empresa <- obtenerEmpresaValida(usuario.identificacion)
      reintentosErroneos <- reglaRepo.getRegla(LlavesReglaContrasena.CANTIDAD_REINTENTOS_INGRESO_CONTRASENA.llave)
      validar <- usuarioAdminRepo.validarUsuario(usuario, contrasena, reintentosErroneos.valor.toInt)
      cliente <- clienteCoreRepo.getCliente(usuario.identificacion)
      estadoCore <- clienteCoreRepo.validarEstado(cliente)

      reglaDias <- reglaRepo.getRegla(LlavesReglaContrasena.DIAS_VALIDA.llave)
      caducidad <- usuarioAdminRepo.validarCaducidadContrasena(TiposCliente.agenteEmpresarial, usuario, reglaDias.valor.toInt)
      actualizar <- usuarioAdminRepo.actualizarInfoUsuario(usuario, ip)


      inactividad <- configuracionRepo.getConfiguracion(TiposConfiguracion.EXPIRACION_SESION.llave)



      token <- generarTokenAdmin(usuario, ip, inactividad.llave)

      ips <- ipRepo.getIpsByEmpresaId(empresa.id)
      validacionIps <- ipRepo.validarControlIpAdmin(ip, ips, token, true)


      asociarToken <- usuarioAdminRepo.actualizarToken(usuario.id, token)
      //TODO: poner la empresa (dto)
      sesion <- actorResponse[SesionActorSupervisor.SesionUsuarioCreada](sessionActor, CrearSesionUsuario(token, inactividad.valor.toInt))

      //validacionPreguntas <- ValidationT(validarPreguntasUsuarioAdmin(usuarioAdmin.id))
      //validacionIps <- ValidationT(validarControlIpsUsuarioEmpresarial(empresa.id, message.clientIp.get, token, usuarioAdmin.tipoIdentificacion, validacionPreguntas))
    }yield
    /*
    val validaciones: Future[Validation[ErrorAutenticacion, String]] = (for {

      } yield validacionIps).run

      validaciones.onComplete {
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
    * */
  }

  private def obtenerEmpresaValida(nit: String): Future[Empresa] = {
    for {
      empresa <- empresaRepo.getByIdentity(nit)
      validar <- empresaRepo.validarEmpresa(empresa)
    } yield empresa.get
  }

  private def generarTokenAgente(usuario: UsuarioEmpresarial, ip: String, inactividad: String): Future[String] = Future {
    Token.generarToken(usuario.nombreUsuario, usuario.correo, getTipoPersona(usuario.tipoIdentificacion),
      usuario.ipUltimoIngreso.get, usuario.fechaUltimoIngreso.getOrElse(new Date(System.currentTimeMillis())),
      inactividad, TiposCliente.agenteEmpresarial, Some(usuario.identificacion))
  }

  private def generarTokenAdmin(usuario: UsuarioEmpresarialAdmin, ip: String, inactividad: String): Future[String] = Future {
    Token.generarToken(usuario.usuario, usuario.correo, getTipoPersona(usuario.tipoIdentificacion),
      usuario.ipUltimoIngreso.get, usuario.fechaUltimoIngreso.getOrElse(new Date(System.currentTimeMillis())),
      inactividad, TiposCliente.agenteEmpresarial, Some(usuario.identificacion))
  }

  private def getTipoPersona(idTipoIdent: Int): String = {
    idTipoIdent match {
      case TipoIdentificacion.FID.identificador => "F"
      case TipoIdentificacion.NIT.identificador => "J"
      case TipoIdentificacion.SOCIEDAD_EXTRANJERA.identificador => "S"
      case _ => "N"
    }
  }

  /**
   * Valida el estado del usuario
   * @param estado del usuario
   * @return Future[Boolean]
   * Success => True
   */
  private def validarEstadoUsuario(estado: Int): Future[Boolean] = {
    if (estado == EstadosEmpresaEnum.bloqueContraseña.id)
      Future.failed(ValidacionException("401.8", "Usuario Bloqueado"))
    else if (estado == EstadosEmpresaEnum.pendienteActivacion.id)
      Future.failed(ValidacionException("401.10", "Usuario Bloqueado"))
    else if (estado == EstadosEmpresaEnum.pendienteReiniciarContrasena.id)
      Future.failed(ValidacionException("401.12", "Usuario Bloqueado"))
    else if (estado == EstadosEmpresaEnum.bloqueadoPorAdmin.id)
      Future.failed(ValidacionException("401.14", "Usuario Desactivado"))
    else Future.successful(true)
  }

}
