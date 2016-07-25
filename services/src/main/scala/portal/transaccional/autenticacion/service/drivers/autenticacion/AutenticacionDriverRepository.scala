package portal.transaccional.autenticacion.service.drivers.autenticacion

import java.util.Date

import co.com.alianza.util.token.Token
import portal.transaccional.autenticacion.service.drivers.usuario.UsuarioRepository

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by hernando on 25/07/16.
 */
case class AutenticacionDriverRepository(usuarioRepo: UsuarioRepository)(implicit val ex: ExecutionContext) extends AutenticacionRepository {

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
  def autenticar(tipoIdentificacion: Int, numeroIdentificacion: String, password: String, clientIp: String): Future[String] = {
    for {
      usuario <- usuarioRepo.getByIdentificacion(numeroIdentificacion)
      estado <- usuarioRepo.validarEstados(usuario.estado)
      token <- Future {
        //Token.generarToken(cliente.wcli_nombre, cliente.wcli_dir_correo, cliente.wcli_person,
        //  usuario.ipUltimoIngreso.getOrElse(clientIp), usuario.fechaUltimoIngreso.getOrElse(new Date(System.currentTimeMillis())), expiracionInactividad)
        Token.generarToken("", "", "",
          usuario.ipUltimoIngreso.getOrElse(clientIp), usuario.fechaUltimoIngreso.getOrElse(new Date(System.currentTimeMillis())), "")

        //usuario <- ValidationT(obtenerUsuario(message.numeroIdentificacion))
        //estadoValido <- ValidationT(validarEstadosUsuario(usuario.estado))
        //passwordValido <- ValidationT(validarPasswords(message.password, usuario.contrasena.getOrElse(""), Some(usuario.identificacion), usuario.id, usuario.numeroIngresosErroneos))
        //cliente <- ValidationT(obtenerClienteSP(usuario.identificacion, usuario.tipoIdentificacion))
        //cienteValido <- ValidationT(validarClienteSP(cliente))
        //passwordCaduco <- ValidationT(validarCaducidadPassword(TiposCliente.clienteIndividual, usuario.id.get, usuario.fechaCaducidad))
        //actualizacionInfo <- ValidationT(actualizarInformacionUsuario(usuario.identificacion, message.clientIp.get))
        //inactividadConfig <- ValidationT(buscarConfiguracion(TiposConfiguracion.EXPIRACION_SESION.llave))
        //token <- ValidationT(generarYAsociarToken(cliente, usuario, inactividadConfig.valor, message.clientIp.get))
        //sesion <- ValidationT(crearSesion(token, inactividadConfig.valor.toInt))
        //validacionPreguntas <- ValidationT(validarPreguntasUsuario(usuario.id.get))
        //validacionIps <- ValidationT(validarControlIpsUsuario(usuario.id.get, message.clientIp.get, token, validacionPreguntas))
      }
    } yield token
  }

  //TODO: Completar
  def autenticarUsuarioEmpresa(tipoIdentificacion: Int, numeroIdentificacion: String, password: String, usuario: String, clientIp: String): Future[String] = {
    for {
      usuario <- usuarioRepo.getUsuarioByIdentificacion(numeroIdentificacion)
      token <- Future {
        //Token.generarToken(cliente.wcli_nombre, cliente.wcli_dir_correo, cliente.wcli_person,
        //  usuario.ipUltimoIngreso.getOrElse(clientIp), usuario.fechaUltimoIngreso.getOrElse(new Date(System.currentTimeMillis())), expiracionInactividad)
        Token.generarToken("", "", "",
          usuario.ipUltimoIngreso.getOrElse(clientIp), usuario.fechaUltimoIngreso.getOrElse(new Date(System.currentTimeMillis())), "")
      }
    } yield token
  }

  ////////// validaciones //////

}

/**
 * usuario <- ValidationT(obtenerUsuario(message.numeroIdentificacion))
 * estadoValido <- ValidationT(validarEstadosUsuario(usuario.estado))
 * passwordValido <- ValidationT(validarPasswords(message.password, usuario.contrasena.getOrElse(""), Some(usuario.identificacion), usuario.id, usuario.numeroIngresosErroneos))
 * cliente <- ValidationT(obtenerClienteSP(usuario.identificacion, usuario.tipoIdentificacion))
 * cienteValido <- ValidationT(validarClienteSP(cliente))
 * passwordCaduco <- ValidationT(validarCaducidadPassword(TiposCliente.clienteIndividual, usuario.id.get, usuario.fechaCaducidad))
 * actualizacionInfo <- ValidationT(actualizarInformacionUsuario(usuario.identificacion, message.clientIp.get))
 * inactividadConfig <- ValidationT(buscarConfiguracion(TiposConfiguracion.EXPIRACION_SESION.llave))
 * token <- ValidationT(generarYAsociarToken(cliente, usuario, inactividadConfig.valor, message.clientIp.get))
 * sesion <- ValidationT(crearSesion(token, inactividadConfig.valor.toInt))
 * validacionPreguntas <- ValidationT(validarPreguntasUsuario(usuario.id.get))
 * validacionIps <- ValidationT(validarControlIpsUsuario(usuario.id.get, message.clientIp.get, token, validacionPreguntas))
 * } yield validacionIps).run
 *
 * validaciones.onComplete {
 * case sFailure(_) => originalSender ! _
 * case sSuccess(resp) => resp match {
 * case zSuccess(token) => originalSender ! token
 * case zFailure(errorAutenticacion) => errorAutenticacion match {
 * case err @ ErrorPersistencia(_, ep1) => originalSender ! ep1
 * case err @ ErrorPasswordInvalido(identificacionUsuario, _, numIngresosErroneosUsuario) =>
 *
 * val ejecucion: Future[Validation[ErrorAutenticacion, Boolean]] = (for {
 * ingresosErroneos <- ValidationT(actualizarIngresosErroneosUsuario(identificacionUsuario.get, numIngresosErroneosUsuario + 1))
 * regla <- ValidationT(buscarRegla("CANTIDAD_REINTENTOS_INGRESO_CONTRASENA"))
 * bloqueo <- ValidationT(bloquearUsuario(identificacionUsuario.get, numIngresosErroneosUsuario, regla))
 * } yield bloqueo).run
 *
 * ejecucion.onFailure { case _ => originalSender ! _ }
 * ejecucion.onSuccess {
 * case zSuccess(_) => originalSender ! ResponseMessage(Unauthorized, err.msg)
 * case zFailure(errorBloqueo) => errorBloqueo match {
 * case errb @ ErrorPersistencia(_, ep2) => originalSender ! ep2
 * case _ => originalSender ! ResponseMessage(Unauthorized, errorBloqueo.msg)
 * }
 * }
 * case _ => originalSender ! ResponseMessage(Unauthorized, errorAutenticacion.msg)
 * }
 * }
 * }
 */
