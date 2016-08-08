package portal.transaccional.autenticacion.service.drivers.autenticacion

import java.sql.Timestamp
import java.util.Date

import akka.util.Timeout
import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.constants.{ LlavesReglaContrasena, TiposConfiguracion }
import co.com.alianza.infrastructure.dto.Cliente
import co.com.alianza.persistence.entities.Usuario
import co.com.alianza.util.token.{ AesUtil, Token }
import portal.transaccional.autenticacion.service.drivers.cliente.ClienteRepository
import portal.transaccional.autenticacion.service.drivers.configuracion.ConfiguracionRepository
import portal.transaccional.autenticacion.service.drivers.ipusuario.IpUsuarioRepository
import portal.transaccional.autenticacion.service.drivers.reglas.ReglaContrasenaRepository
import portal.transaccional.autenticacion.service.drivers.respuesta.RespuestaUsuarioRepository
import portal.transaccional.autenticacion.service.drivers.sesion.SesionRepository
import portal.transaccional.autenticacion.service.drivers.usuarioIndividual.UsuarioRepository

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

case class AutenticacionDriverRepository(usuarioRepo: UsuarioRepository, clienteCoreRepo: ClienteRepository,
    configuracionRepo: ConfiguracionRepository, reglaRepo: ReglaContrasenaRepository, ipRepo: IpUsuarioRepository,
    respuestasRepo: RespuestaUsuarioRepository, sesionRepo: SesionRepository)(implicit val ex: ExecutionContext) extends AutenticacionRepository {

  implicit val timeout = Timeout(5.seconds)

  /**
   * Flujo:
   * - obtener usuario
   * - validar estado
   * - obtener regla de reintentos
   * - validar contrasena
   * - obtener cliente core
   * - validar estado cliente core
   * - obtener regla dias
   * - validar caducidad
   * - actualizar ingresos erroneos
   * - actualizar ip
   * - actualizar fecha
   * - obtener configuracion inactividad
   * - generar token
   * - asociar token
   * - crear session de usuario
   * - obtener respuestas
   * - obtener ips
   * - validar ip y respuestas
   */
  def autenticar(tipoIdentificacion: Int, numeroIdentificacion: String, contrasena: String, ip: String): Future[String] = {
    for {
      usuario <- usuarioRepo.getByIdentificacion(numeroIdentificacion)
      estado <- usuarioRepo.validarEstado(usuario.estado)
      reintentosErroneos <- reglaRepo.getRegla(LlavesReglaContrasena.CANTIDAD_REINTENTOS_INGRESO_CONTRASENA.llave)
      contrasena <- usuarioRepo.validarContrasena(contrasena, usuario, reintentosErroneos.valor.toInt)
      cliente <- clienteCoreRepo.getCliente(numeroIdentificacion)
      estadoCore <- clienteCoreRepo.validarEstado(cliente)
      reglaDias <- reglaRepo.getRegla(LlavesReglaContrasena.DIAS_VALIDA.llave)
      validarCaducidad <- usuarioRepo.validarCaducidadContrasena(TiposCliente.clienteIndividual, usuario, reglaDias.valor.toInt)
      ingErroneos <- usuarioRepo.actualizarIngresosErroneosUsuario(usuario.id.get, 0)
      actualizarIP <- usuarioRepo.actualizarIp(numeroIdentificacion, ip)
      fechaUltimoIngreso <- usuarioRepo.actualizarFechaIngreso(numeroIdentificacion, new Timestamp((new Date).getTime))
      inactividad <- configuracionRepo.getConfiguracion(TiposConfiguracion.EXPIRACION_SESION.llave)
      token <- generarToken(usuario, cliente, ip, inactividad.valor)
      asociarToken <- usuarioRepo.actualizarToken(numeroIdentificacion, AesUtil.encriptarToken(token))
      rsp <- sesionRepo.crearSesion(token, inactividad.valor.toInt, None)
      respuestas <- respuestasRepo.getRespuestasById(usuario.id.get)
      ips <- ipRepo.getIpsUsuarioById(usuario.id.get)
      validacionIps <- ipRepo.validarControlIp(ip, ips, token, respuestas.nonEmpty)
    } yield validacionIps
  }

  /**
   * Generar token
   * @param usuario Usuario que accede al servicio
   * @param cliente  Cliente
   * @param ip Ip por la cual accede el usuario
   * @param inactividad Inactividad del usuario
   * @return
   */
  private def generarToken(usuario: Usuario, cliente: Cliente, ip: String, inactividad: String): Future[String] = Future {
    Token.generarToken(cliente.wcli_nombre, cliente.wcli_dir_correo, cliente.wcli_person,
      usuario.ipUltimoIngreso.getOrElse(ip), usuario.fechaUltimoIngreso.getOrElse(new Date(System.currentTimeMillis())), inactividad)
  }

}
