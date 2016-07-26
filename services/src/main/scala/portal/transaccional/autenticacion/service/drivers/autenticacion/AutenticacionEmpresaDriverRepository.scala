package portal.transaccional.autenticacion.service.drivers.autenticacion

import portal.transaccional.autenticacion.service.drivers.usuario.{ UsuarioEmpresarialAdminRepository, UsuarioEmpresarialRepository }

import scala.concurrent.Future

/**
 * Created by hernando on 26/07/16.
 */
case class AutenticacionEmpresaDriverRepository(
    usuarioRepo: UsuarioEmpresarialRepository, usuarioAdminRepo: UsuarioEmpresarialAdminRepository
) extends AutenticacionEmpresaRepository {

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
  def autenticarUsuarioEmpresa(tipoIdentificacion: Int, numeroIdentificacion: String,
    usuario: String, password: String, clientIp: String): Future[String] = {
    //TODO: realizar el flujo descrito
    Future.successful("")
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
  private def autenticarAgente(): Future[String] = {
    for {
      //estadoEmpresaOk <- ValidationT(validarEstadoEmpresa(message.nit))
      //usuarioAgente <- ValidationT(obtenerUsuarioEmpresarialAgente(message.nit, message.usuario))
      //estadoValido <- ValidationT(validarEstadosUsuario(usuarioAgente.estado))
      //passwordValido <- ValidationT(validarPasswords(message.password, usuarioAgente.contrasena.getOrElse(""), None, Some(usuarioAgente.id), usuarioAgente.numeroIngresosErroneos))
      //passwordCaduco <- ValidationT(validarCaducidadPassword(TiposCliente.agenteEmpresarial, usuarioAgente.id, usuarioAgente.fechaCaducidad))
      //actualizacionInfo <- ValidationT(actualizarInformacionUsuarioEmpresarialAgente(usuarioAgente.id, message.clientIp.get))
      //inactividadConfig <- ValidationT(buscarConfiguracion(TiposConfiguracion.EXPIRACION_SESION.llave))
      //token <- ValidationT(generarYAsociarTokenUsuarioEmpresarialAgente(usuarioAgente, message.nit, inactividadConfig.valor, message.clientIp.get))
      //empresa <- ValidationT(obtenerEmpresaPorNit(message.nit))
      //sesion <- ValidationT(crearSesion(token, inactividadConfig.valor.toInt, empresa, None))
      //validacionIps <- ValidationT(validarControlIpsUsuarioEmpresarial(empresa.id, message.clientIp.get, token, usuarioAgente.tipoIdentificacion, "true"))

      token <- Some("token")
    } yield token
    /*
    def validaciones: Future[Validation[ErrorAutenticacion, String]] = (for {
        estadoEmpresaOk <- ValidationT(validarEstadoEmpresa(message.nit))
        usuarioAgente <- ValidationT(obtenerUsuarioEmpresarialAgente(message.nit, message.usuario))
        estadoValido <- ValidationT(validarEstadosUsuario(usuarioAgente.estado))
        passwordValido <- ValidationT(validarPasswords(message.password, usuarioAgente.contrasena.getOrElse(""), None, Some(usuarioAgente.id), usuarioAgente.numeroIngresosErroneos))
        passwordCaduco <- ValidationT(validarCaducidadPassword(TiposCliente.agenteEmpresarial, usuarioAgente.id, usuarioAgente.fechaCaducidad))
        actualizacionInfo <- ValidationT(actualizarInformacionUsuarioEmpresarialAgente(usuarioAgente.id, message.clientIp.get))
        inactividadConfig <- ValidationT(buscarConfiguracion(TiposConfiguracion.EXPIRACION_SESION.llave))
        token <- ValidationT(generarYAsociarTokenUsuarioEmpresarialAgente(usuarioAgente, message.nit, inactividadConfig.valor, message.clientIp.get))
        empresa <- ValidationT(obtenerEmpresaPorNit(message.nit))
        sesion <- ValidationT(crearSesion(token, inactividadConfig.valor.toInt, empresa, None))
        validacionIps <- ValidationT(validarControlIpsUsuarioEmpresarial(empresa.id, message.clientIp.get, token, usuarioAgente.tipoIdentificacion, "true"))
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
    */
    Future.successful("")
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
  private def autenticarAdministrador(): Future[String] = {
    /*
    val validaciones: Future[Validation[ErrorAutenticacion, String]] = (for {
        estadoEmpresaOk <- ValidationT(validarEstadoEmpresa(message.nit))
        usuarioAdmin <- ValidationT(obtenerUsuarioEmpresarialAdmin(message.nit, message.usuario))
        estadoValido <- ValidationT(validarEstadosUsuario(usuarioAdmin.estado))
        passwordValido <- ValidationT(validarPasswords(message.password, usuarioAdmin.contrasena.getOrElse(""), None, Some(usuarioAdmin.id), usuarioAdmin.numeroIngresosErroneos))
        cliente <- ValidationT(obtenerClienteSP(usuarioAdmin.identificacion, usuarioAdmin.tipoIdentificacion))
        cienteValido <- ValidationT(validarClienteSP(cliente))
        passwordCaduco <- ValidationT(validarCaducidadPassword(TiposCliente.clienteAdministrador, usuarioAdmin.id, usuarioAdmin.fechaCaducidad))
        actualizacionInfo <- ValidationT(actualizarInformacionUsuarioEmpresarialAdmin(usuarioAdmin.id, message.clientIp.get))
        inactividadConfig <- ValidationT(buscarConfiguracion(TiposConfiguracion.EXPIRACION_SESION.llave))
        token <- ValidationT(generarYAsociarTokenUsuarioEmpresarialAdmin(cliente, usuarioAdmin, message.nit, inactividadConfig.valor, message.clientIp.get))
        sesion <- ValidationT(crearSesion(token, inactividadConfig.valor.toInt))
        empresa <- ValidationT(obtenerEmpresaPorNit(message.nit))
        validacionPreguntas <- ValidationT(validarPreguntasUsuarioAdmin(usuarioAdmin.id))
        validacionIps <- ValidationT(validarControlIpsUsuarioEmpresarial(empresa.id, message.clientIp.get, token, usuarioAdmin.tipoIdentificacion, validacionPreguntas))
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
    Future.successful("")
  }

}
