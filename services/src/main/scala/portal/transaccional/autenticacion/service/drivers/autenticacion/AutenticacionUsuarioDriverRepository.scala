package portal.transaccional.autenticacion.service.drivers.autenticacion

import java.sql.Timestamp
import java.util.Date

import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.commons.enumerations.TiposCliente.TiposCliente
import co.com.alianza.exceptions.ValidacionException
import co.com.alianza.persistence.entities.{ PerfilLdap, PerfilUsuario, Usuario, ValidacionPerfil }
import co.com.alianza.util.token.Token
import enumerations.{ ConfiguracionEnum, TipoIngresoUsuario }
import enumerations.empresa.TipoValidacion
import portal.transaccional.autenticacion.service.drivers.configuracion.ConfiguracionRepository
import portal.transaccional.autenticacion.service.drivers.ldap.LdapRepository
import portal.transaccional.autenticacion.service.drivers.sesion.SesionRepository
import portal.transaccional.autenticacion.service.drivers.usuarioIndividual.UsuarioRepository
import portal.transaccional.autenticacion.service.drivers.validacion.ValidacionPerfilRepository
import portal.transaccional.autenticacion.service.web.autenticacion.UsuarioGenRequest
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.{ AlianzaDAOs, PerfilUsuarioDAOs }

import scala.concurrent.{ ExecutionContext, Future }
/**OFV LOGIN FASE 1**/
case class AutenticacionUsuarioDriverRepository(
    validacionPerfilRepo: ValidacionPerfilRepository,
    usuarioRepository: UsuarioRepository,
    ldapRepository: LdapRepository,
    perfilUsuarioDAO: PerfilUsuarioDAOs,
    configuracionRepo: ConfiguracionRepository,
    sesionRepository: SesionRepository,
    alianzaDAO: AlianzaDAOs
)(implicit val ex: ExecutionContext) extends AutenticacionUsuarioRepository {
  /**
   * Autenticación general para usuario
   * @param usuarioGenRequest Peticion usuario
   * @param ip Ip donde llega la petición.
   * @return
   */
  def autenticarGeneral(usuarioGenRequest: UsuarioGenRequest, ip: String): Future[String] = {
    var datosIngreso = usuarioGenRequest
    if (usuarioGenRequest.tipoIngreso == TipoIngresoUsuario.BD_PORTAL.idIngreso) {
      for {
        usuario <- usuarioRepository.getByUsuario(datosIngreso)
        perfil <- perfilUsuarioDAO.getProfileByUsuario(usuario.id.get)
        validacionesPerfil <- validacionPerfilRepo.getValidacion(perfil.get.idPerfil)
        validacion <- ejecutarValidacionesPerfil(validacionesPerfil, usuario, datosIngreso.password, ip, perfil.get)
      } yield validacion
    } else {
      var tipocliente: TiposCliente =
        if (usuarioGenRequest.tipoIngreso == TipoIngresoUsuario.LDAP_ALIANZA.idIngreso) {
          TiposCliente.ldapGen
        } else {
          TiposCliente.comercialSAC
        }
      for {
        usuarioLdap <- ldapRepository.autenticarLdap(datosIngreso.usuario.get, tipocliente, datosIngreso.password)
        perfil <- alianzaDAO.getProfileByLdap(usuarioLdap.perfilLdap.get)
        perfil <- validarPerfilLdap(perfil)
        creado <- crearUsuarioLdap(usuarioLdap.usuario, usuarioLdap.mail.getOrElse("sinemail@alianza.com.co"))
        inactividad <- configuracionRepo.getConfiguracion(ConfiguracionEnum.EXPIRACION_SESION)
        token <- generarTokenLdap(usuarioLdap.usuario, ip, inactividad.valor, perfil.get.perfilPortal, Some(creado))
        rsp <- sesionRepository.crearSesion(token, inactividad.valor.toInt, None)
        encriptedToken <- validacionPerfilRepo.noValidaIp(token)
      } yield encriptedToken
    }
  }

  /**
   * Realiza la ejecución dinamica de las validaciones asociadas a los perfiles
   * @param validacionesPerfil Validaciones a ejecutar.
   * @param usuario Datos del usuario en base de datos
   * @param clave Contraseña usuario
   * @param ip Ip donde se genera la peticion
   * @param perfilUsuario Perfil asociado al usuario
   * @return Si las validaciones son exitosas devuelve el token.
   */
  private def ejecutarValidacionesPerfil(validacionesPerfil: Seq[ValidacionPerfil], usuario: Usuario, clave: String,
    ip: String, perfilUsuario: PerfilUsuario): Future[String] = {

    var validaIp = false
    var validaReintento = false
    validacionesPerfil.foreach(
      validacion =>
        if (validacion.tipoValidacion == TipoValidacion.CADUCIDAD_PASS.idT)
          for (rta <- validacionPerfilRepo.validarCaducidad(usuario)) yield rta
        else if (validacion.tipoValidacion == TipoValidacion.ESTADO_SIFI.idT)
          for (rta <- validacionPerfilRepo.validarEstadoSifi(usuario.identificacion, usuario.id)) yield rta
        else if (validacion.tipoValidacion == TipoValidacion.IP_CONFIANZA.idT)
          validaIp = true
        else if (validacion.tipoValidacion == TipoValidacion.REINTENTOS.idT)
          validaReintento = true
    )

    for {
      okey <- (if (validaReintento) validacionPerfilRepo.validarPassConReintento(clave, usuario) else validacionPerfilRepo.validarPassSinReintento(clave, usuario))
      inactividad <- configuracionRepo.getConfiguracion(ConfiguracionEnum.EXPIRACION_SESION)
      token <- generarToken(usuario, ip, inactividad.valor, perfilUsuario)
      rsp <- sesionRepository.crearSesion(token, inactividad.valor.toInt, None)
      tokenEncripted <- (if (validaIp) validacionPerfilRepo.validarIpConfianza(usuario.id.get, token, ip) else validacionPerfilRepo.noValidaIp(token))
    } yield tokenEncripted
  }

  /**
   * Generar token
   * @param usuario Usuario que accede al servicio
   * @param ip Ip por la cual accede el usuario
   * @param inactividad Inactividad del usuario
   * @return
   */
  private def generarToken(usuario: Usuario, ip: String, inactividad: String, perfilUsuario: PerfilUsuario): Future[String] = Future {
    Token.generarTokenGeneral(usuario.correo, usuario.correo, usuario.tipoIdentificacion.toString,
      usuario.ipUltimoIngreso.getOrElse(ip), usuario.fechaUltimoIngreso.getOrElse(new Date(System.currentTimeMillis())), inactividad, perfilUsuario.idPerfil.toString)
  }

  /**
   * Generar token para usuario LDAP
   * @param usuario Usuario que accede al servicio
   * @param ip Ip por la cual accede el usuario
   * @param inactividad Inactividad del usuario
   * @return
   */
  private def generarTokenLdap(usuario: String, ip: String, inactividad: String, perfilUsuario: Int, idUsuario: Option[Int] = None): Future[String] = Future {
    Token.generarTokenGeneral(usuario, usuario, usuario, ip, (new Date(System.currentTimeMillis())), inactividad, perfilUsuario.toString, None, idUsuario)
  }

  private def crearUsuarioLdap(usuario: String, email: String): Future[Int] = {
    var usuarioObj: Usuario =
      Usuario(
        None, //id
        email, //email
        new Timestamp(new Date().getTime), //fechaActualizacion
        usuario, //identificacion
        1, //TipoIdentificacion
        1, //Estado
        None, None, 0, None, None, Some(usuario)
      )
    usuarioRepository.createIfNotExist(usuarioObj)
  }

  /**
   *
   * @param perfilLdap
   * @return
   */
  private def validarPerfilLdap(perfilLdap: Option[PerfilLdap]): Future[Option[PerfilLdap]] = {
    perfilLdap match {
      case Some(pefil: PerfilLdap) => Future.successful(perfilLdap)
      case (None) => Future.failed(ValidacionException("401.1", "Usuario no autorizado"))
    }
  }
}
