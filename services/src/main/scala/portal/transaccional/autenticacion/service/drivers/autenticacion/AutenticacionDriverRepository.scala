package portal.transaccional.autenticacion.service.drivers.autenticacion

import java.sql.Timestamp
import java.util.Date

import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.constants.{ LlavesReglaContrasena, TiposConfiguracion }
import co.com.alianza.infrastructure.dto.Cliente
import co.com.alianza.persistence.entities.Usuario
import co.com.alianza.util.token.Token
import portal.transaccional.autenticacion.service.drivers.cliente.ClienteRepository
import portal.transaccional.autenticacion.service.drivers.configuracion.ConfiguracionRepository
import portal.transaccional.autenticacion.service.drivers.ipusuario.IpUsuarioRepository
import portal.transaccional.autenticacion.service.drivers.reglas.ReglaContrasenaRepository
import portal.transaccional.autenticacion.service.drivers.usuario.UsuarioRepository

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by hernando on 25/07/16.
 */
case class AutenticacionDriverRepository(usuarioRepo: UsuarioRepository, clienteCoreRepo: ClienteRepository, configuracionRepo: ConfiguracionRepository,
    reglaRepo: ReglaContrasenaRepository, ipRepo: IpUsuarioRepository)(implicit val ex: ExecutionContext) extends AutenticacionRepository {

  /**
   * Flujo:
   * 1) Busca el usuario en la base de datos, si no se encuentra se devuelve CredencialesInvalidas
   * 2) Valida los estados del usuario encontrado, esta validacion devuelve un tipo de error por estado, si es exitosa se continúa el proceso
   * 3) Se comparan los passwords de la petición y el usuario, si coinciden se prosigue de lo contrario se debe ejecutar la excepcion de pw inválido
   * 4) Se busca el cliente en el core de alianza, si no se encuentra se debe devolver ErrorClienteNoExisteCore
   * 5) Se valida el cliente encontrado, este metodo devuelve un error de la validacion que no cumple
   * 6) Se valida la fecha de caducacion del password, si caducó se debe devolver ErrorPasswordCaducado, de lo contrario se prosigue
   * ------- Si pasan las 6 validaciones anteriores, el usuario se considera como usuario autenticado --------
   * 7) Se actualiza la información de numIngresosErroneos
   * 8) ipUltimoIngreso
   * 9) fechaUltimoIngreso del usuario
   * 10) Se asigna el tiempo de expiración
   * 11) Se genera un token
   * 12) se asocia al usuario(token)
   * 13) Se crea la sesion del usuario en el cluster
   * 14) Se valida si el usuario tiene alguna ip guardada, si es así se procede a validar si es una ip habitual, de lo contrario se genera un token (10), una sesion (11) y se responde con ErrorControlIpsDesactivado
   */
  def autenticar(tipoIdentificacion: Int, numeroIdentificacion: String, contrasena: String, ip: String): Future[String] = {
    for {
      usuario <- usuarioRepo.getByIdentificacion(numeroIdentificacion)
      estado <- usuarioRepo.validarEstados(usuario.estado)
      contrasena <- usuarioRepo.validarContrasena(contrasena, usuario.contrasena.get, usuario.id.get)
      cliente <- clienteCoreRepo.getCliente(numeroIdentificacion)
      estadoCore <- clienteCoreRepo.validarEstado(cliente)
      reglaDias <- reglaRepo.getRegla(LlavesReglaContrasena.DIAS_VALIDA.llave)
      validarCaducidad <- usuarioRepo.validarCaducidadContrasena(TiposCliente.clienteIndividual, usuario, reglaDias.valor.toInt)
      ingErroneos <- usuarioRepo.actualizarIngresosErroneosUsuario(usuario.id.get, 0)
      actualizarIP <- usuarioRepo.actualizarIp(numeroIdentificacion, ip)
      fechaUltimoIngreso <- usuarioRepo.actualizarFechaIngreso(numeroIdentificacion, new Timestamp((new Date).getTime))
      inactividad <- configuracionRepo.getConfiguracion(TiposConfiguracion.EXPIRACION_SESION.llave)
      token <- generarToken(usuario, cliente, ip, inactividad.valor)
      asociarToken <- usuarioRepo.actualizarToken(numeroIdentificacion, token)
      //sesion <- ValidationT(crearSesion(token, inactividadConfig.valor.toInt))

      tieneRespuestas <- Future { true }
      //validacionPreguntas <- ValidationT(validarPreguntasUsuario(usuario.id.get))
      //TODO: depende del repo preguntas

      ips <- ipRepo.getIpsUsuarioById(usuario.id.get)
      validacionIps <- ipRepo.validarControlIp(ip, ips, token, tieneRespuestas)
    } yield token
  }

  /**
   * Generar token
   * @param usuario
   * @param cliente
   * @param ip
   * @param inactividad
   * @return
   */
  private def generarToken(usuario: Usuario, cliente: Cliente, ip: String, inactividad: String): Future[String] = Future {
    Token.generarToken(cliente.wcli_nombre, cliente.wcli_dir_correo, cliente.wcli_person,
      usuario.ipUltimoIngreso.getOrElse(ip), usuario.fechaUltimoIngreso.getOrElse(new Date(System.currentTimeMillis())), inactividad)
  }

  //TODO: Completar
  def autenticarUsuarioEmpresa(tipoIdentificacion: Int, numeroIdentificacion: String, password: String, usuario: String, clientIp: String): Future[String] = {
    for {
      usuario <- usuarioRepo.getByIdentificacion(numeroIdentificacion)
      token <- Future {
        //Token.generarToken(cliente.wcli_nombre, cliente.wcli_dir_correo, cliente.wcli_person,
        //  usuario.ipUltimoIngreso.getOrElse(clientIp), usuario.fechaUltimoIngreso.getOrElse(new Date(System.currentTimeMillis())), expiracionInactividad)
        Token.generarToken("", "", "",
          usuario.ipUltimoIngreso.getOrElse(clientIp), usuario.fechaUltimoIngreso.getOrElse(new Date(System.currentTimeMillis())), "")
      }
    } yield token
  }

}
