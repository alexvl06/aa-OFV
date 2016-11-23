package portal.transaccional.autenticacion.service.drivers.autenticacion

import java.util.Date

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.constants.LlavesReglaContrasena
import co.com.alianza.exceptions.ValidacionException
import co.com.alianza.infrastructure.messages.CrearSesionUsuario
import co.com.alianza.persistence.entities.{ Empresa, UsuarioAgenteEmpresarial, UsuarioEmpresarialAdmin }
import co.com.alianza.util.token.{ AesUtil, Token }
import enumerations.{ ConfiguracionEnum, EstadosEmpresaEnum, TipoIdentificacion }
import portal.transaccional.autenticacion.service.drivers.cliente.ClienteRepository
import portal.transaccional.autenticacion.service.drivers.configuracion.ConfiguracionRepository
import portal.transaccional.autenticacion.service.drivers.empresa.{ EmpresaRepository, DataAccessTranslator => EmpresaDTO }
import portal.transaccional.autenticacion.service.drivers.ipempresa.IpEmpresaRepository
import portal.transaccional.autenticacion.service.drivers.reglas.ReglaContrasenaRepository
import portal.transaccional.autenticacion.service.drivers.respuesta.RespuestaUsuarioRepository
import portal.transaccional.autenticacion.service.drivers.sesion.SesionRepository
import portal.transaccional.autenticacion.service.drivers.usuarioAdmin.UsuarioAdminRepository
import portal.transaccional.autenticacion.service.drivers.usuarioAgente.UsuarioAgenteEmpresarialRepository

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }
import scala.reflect.ClassTag

case class AutenticacionEmpresaDriverRepository(
    usuarioRepo: UsuarioAgenteEmpresarialRepository, usuarioAdminRepo: UsuarioAdminRepository, clienteCoreRepo: ClienteRepository,
    empresaRepo: EmpresaRepository, reglaRepo: ReglaContrasenaRepository, configuracionRepo: ConfiguracionRepository, ipRepo: IpEmpresaRepository,
    sesionRepo: SesionRepository, respuestasRepo: RespuestaUsuarioRepository
)(implicit val ex: ExecutionContext) extends AutenticacionEmpresaRepository {

  implicit val timeout = Timeout(10.seconds)

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
  private def autenticar(agente: Option[UsuarioAgenteEmpresarial], admin: Option[UsuarioEmpresarialAdmin], contrasena: String, ip: String): Future[String] = {
    if (agente.isDefined) {
      autenticarAgente(agente.get, contrasena, ip)
    } else if (admin.isDefined) {
      autenticarAdministrador(admin.get, contrasena, ip)
    } else {
      Future.failed(ValidacionException("401.3", "Error Credenciales"))
    }
  }

  /**
   * Flujo:
   * - buscar y validar empresa
   * - obtener usuario
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
  private def autenticarAgente(usuario: UsuarioAgenteEmpresarial, contrasena: String, ip: String): Future[String] = {
    for {
      empresa <- obtenerEmpresaValida(usuario.identificacion)
      reintentosErroneos <- reglaRepo.getRegla(LlavesReglaContrasena.CANTIDAD_REINTENTOS_INGRESO_CONTRASENA)
      validar <- usuarioRepo.validarUsuario(usuario, contrasena, reintentosErroneos.valor.toInt)
      cliente <- clienteCoreRepo.getCliente(usuario.identificacion, Some(usuario.tipoIdentificacion))
      estadoCore <- clienteCoreRepo.validarEstado(cliente)
      reglaDias <- reglaRepo.getRegla(LlavesReglaContrasena.DIAS_VALIDA)
      caducidad <- usuarioRepo.validarCaducidadContrasena(TiposCliente.agenteEmpresarial, usuario, reglaDias.valor.toInt)
      actualizar <- usuarioRepo.actualizarInfoUsuario(usuario, ip)
      inactividad <- configuracionRepo.getConfiguracion(ConfiguracionEnum.EXPIRACION_SESION)
      token <- generarTokenAgente(usuario, ip, inactividad.valor)
      ips <- ipRepo.getIpsByEmpresaId(empresa.id)
      validacionIps <- ipRepo.validarControlIpAgente(ip, ips, token)
      asociarToken <- usuarioRepo.actualizarToken(usuario.id, AesUtil.encriptarToken(token))
      sesion <- sesionRepo.crearSesion(token, inactividad.valor.toInt, Option(EmpresaDTO.entityToDto(empresa)))
    } yield token
  }

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
   * - obtener configuracion inactividad
   * - generar token
   * - obtener ips
   * - obtener respuestas
   * - validar ips
   * - asociar token
   * - crear session de usuario
   */
  private def autenticarAdministrador(usuario: UsuarioEmpresarialAdmin, contrasena: String, ip: String): Future[String] = {
    for {
      empresa <- obtenerEmpresaValida(usuario.identificacion)
      reintentosErroneos <- reglaRepo.getRegla(LlavesReglaContrasena.CANTIDAD_REINTENTOS_INGRESO_CONTRASENA)
      validar <- usuarioAdminRepo.validarUsuario(usuario, contrasena, reintentosErroneos.valor.toInt)
      cliente <- clienteCoreRepo.getCliente(usuario.identificacion, Some(usuario.tipoIdentificacion))
      estadoCore <- clienteCoreRepo.validarEstado(cliente)
      reglaDias <- reglaRepo.getRegla(LlavesReglaContrasena.DIAS_VALIDA)
      caducidad <- usuarioAdminRepo.validarCaducidadContrasena(TiposCliente.clienteAdministrador, usuario, reglaDias.valor.toInt)
      actualizar <- usuarioAdminRepo.actualizarInfoUsuario(usuario, ip)
      inactividad <- configuracionRepo.getConfiguracion(ConfiguracionEnum.EXPIRACION_SESION)
      token <- generarTokenAdmin(usuario, ip, inactividad.valor)
      sesion <- sesionRepo.crearSesion(token, inactividad.valor.toInt, Option(EmpresaDTO.entityToDto(empresa)))
      asociarToken <- usuarioAdminRepo.actualizarToken(usuario.id, AesUtil.encriptarToken(token))
      respuestas <- respuestasRepo.getRespuestasById(usuario.id)
      ips <- ipRepo.getIpsByEmpresaId(empresa.id)
      validacionIps <- ipRepo.validarControlIpAdmin(ip, ips, token, respuestas.nonEmpty)
    } yield token
  }

  private def mensajeCrearSesion(token: String, inactividad: Int, empresa: Empresa) = {
    CrearSesionUsuario(token, inactividad, Option(EmpresaDTO.entityToDto(empresa)))
  }

  private def actorResponse[T: ClassTag](actor: ActorRef, msg: CrearSesionUsuario): Future[T] = {
    (actor ? msg).mapTo[T]
  }

  private def obtenerEmpresaValida(nit: String): Future[Empresa] = {
    for {
      empresa <- empresaRepo.getByIdentity(nit)
      validar <- empresaRepo.validarEmpresa(empresa)
    } yield empresa.get
  }

  private def generarTokenAgente(usuario: UsuarioAgenteEmpresarial, ip: String, inactividad: String): Future[String] = Future {
    Token.generarToken(usuario.nombreUsuario, usuario.correo, getTipoPersona(usuario.tipoIdentificacion),
      usuario.ipUltimoIngreso.get, usuario.fechaUltimoIngreso.getOrElse(new Date(System.currentTimeMillis())),
      inactividad, TiposCliente.agenteEmpresarial, Some(usuario.identificacion))
  }

  private def generarTokenAdmin(usuario: UsuarioEmpresarialAdmin, ip: String, inactividad: String): Future[String] = Future {
    Token.generarToken(usuario.usuario, usuario.correo, getTipoPersona(usuario.tipoIdentificacion),
      usuario.ipUltimoIngreso.getOrElse(""), usuario.fechaUltimoIngreso.getOrElse(new Date(System.currentTimeMillis())),
      inactividad, TiposCliente.clienteAdministrador, Some(usuario.identificacion))
  }

  private def getTipoPersona(idTipoIdent: Int): String = {
    idTipoIdent match {
      case TipoIdentificacion.FID.identificador => "F"
      case TipoIdentificacion.GRUPO.identificador => "G"
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
