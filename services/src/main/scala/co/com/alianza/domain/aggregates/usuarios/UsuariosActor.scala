package co.com.alianza.domain.aggregates.usuarios

import java.sql.Timestamp
import java.util.Calendar

import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import akka.routing.RoundRobinPool
import co.cifin.confrontaultra.dto.ultra.{ CuestionarioULTRADTO, ParametrosSeguridadULTRADTO, ParametrosULTRADTO, ResultadoEvaluacionCuestionarioULTRADTO }
import co.com.alianza.exceptions.{ BusinessLevel, PersistenceException }
import co.com.alianza.infrastructure.anticorruption.usuarios.{ DataAccessAdapter => DataAccessAdapterUsuario }
import co.com.alianza.infrastructure.dto.{ Empresa, _ }
import co.com.alianza.infrastructure.messages.{ OlvidoContrasenaMessage, ResponseMessage, UsuarioMessage, _ }
import co.com.alianza.microservices.{ MailMessage, SmtpServiceClient }
import co.com.alianza.persistence.entities.{ Configuracion => _, Usuario => _, UsuarioAgenteInmobiliario => _, UsuarioEmpresarialAdmin => _, _ }
import co.com.alianza.util.json.JsonUtil
import co.com.alianza.util.json.MarshallableImplicits._
import co.com.alianza.util.token.{ PinData, TokenPin }
import co.com.alianza.util.transformers.ValidationT
import com.asobancaria.cifinpruebas.cifin.confrontav2plusws.services.ConfrontaUltraWS.{ ConfrontaUltraWSSoapBindingStub, ConfrontaUltraWebServiceServiceLocator }
import com.typesafe.config.Config
import enumerations.empresa.EstadosDeEmpresaEnum
import enumerations.{ ConfiguracionEnum, EstadosEmpresaEnum, EstadosUsuarioEnum, EstadosUsuarioEnumInmobiliario }
import portal.transaccional.autenticacion.service.drivers.usuarioAgenteInmobiliario.UsuarioInmobiliarioPinRepository
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.{ ConfiguracionDAOs, UsuarioAgenteInmobDAOs }
import spray.http.StatusCodes._

import scalaz.std.AllInstances._
import scala.concurrent.Future
import scala.util.{ Failure => sFailure, Success => sSuccess }
import scalaz.Validation.FlatMap._
import scalaz.{ Validation, Failure => zFailure, Success => zSuccess }

class UsuariosActorSupervisor(
    agentesInmobDao: UsuarioAgenteInmobDAOs,
    agentesInmobPinRepo: UsuarioInmobiliarioPinRepository,
    configDao: ConfiguracionDAOs
)(implicit config: Config) extends Actor with ActorLogging {

  import akka.actor.OneForOneStrategy
  import akka.actor.SupervisorStrategy._

  val usuariosActor = context.actorOf(Props(UsuariosActor(agentesInmobDao, agentesInmobPinRepo, configDao))
    .withRouter(RoundRobinPool(nrOfInstances = 2)), "usuariosActor")
  val usuarioEmpresarialActor = context.actorOf(Props[UsuarioEmpresarialActor]
    .withRouter(RoundRobinPool(nrOfInstances = 2)), "usuarioEmpresarialActor")

  def receive = {
    case message: ConsultaUsuarioEmpresarialMessage =>
      usuarioEmpresarialActor forward message
    case message: ConsultaUsuarioEmpresarialAdminMessage =>
      usuarioEmpresarialActor forward message
    case message: Any =>
      usuariosActor forward message
  }

  override val supervisorStrategy = OneForOneStrategy() {
    case exception: Exception =>
      exception.printStackTrace()
      log.error(exception, exception.getMessage)
      Restart
  }
}

case class UsuariosActor(
    agentesInmobDao: UsuarioAgenteInmobDAOs,
    agentesInmobPinRepo: UsuarioInmobiliarioPinRepository,
    configDao: ConfiguracionDAOs
)(implicit config: Config) extends Actor with ActorLogging {

  import co.com.alianza.domain.aggregates.usuarios.ValidacionesUsuario._

  def receive = {
    case message: UsuarioMessage =>
      val currentSender = sender()
      val crearUsuarioFuture = (for {
        validacionIpConfianza <- ValidationT(validaIpConfianza(message.activarIP))
        captchaVal <- ValidationT(validaCaptcha(message))
        cliente <- ValidationT(validaSolicitud(message))
      } yield {
        cliente
      }).run
      resolveCrearUsuarioFuture(crearUsuarioFuture, currentSender, message)

    case message: DesbloquearMessage =>
      val currentSender = sender()
      //obtener el usuairo
      val futureCliente = (for {
        usuario <- ValidationT(validacionUsuarioNumDoc(message.identificacion))
        cliente <- ValidationT(validacionConsultaCliente(usuario.get.identificacion, usuario.get.tipoIdentificacion, true))
      } yield {
        (usuario, cliente)
      }).run
      resolveDesbloquearContrasenaFuture(futureCliente, currentSender, message)

    case message: OlvidoContrasenaMessage =>
      val currentSender = sender()
      val msg: OlvidoContrasenaMessage = message
      //Se obtiene el usuario dado el perfil que llegue de presentacion, en caso de perfil no correcto se devuelve excepcion
      val futureConsultaUsuarios: Future[Validation[PersistenceException, Option[Any]]] = (message.perfilCliente match {
        case 1 => co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter.obtenerUsuarioNumeroIdentificacion(message.identificacion)
        case 2 => co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter.obtieneUsuarioEmpresarialAdminPorNitYUsuario(
          message.identificacion,
          message.usuarioClienteAdmin.get
        )
        case _ => Future.successful(Validation.failure(PersistenceException(new Exception, BusinessLevel,
          "El perfil del usuario no es soportado por la aplicacion")))
      }).flatMap(res => buscarAgenteInmobiliario(res, msg.identificacion, msg.usuarioClienteAdmin.getOrElse("")))

      val validarClienteFuture = (for {
        cliente <- ValidationT(validaSolicitudCliente(message.identificacion, message.tipoIdentificacion))
      } yield {
        cliente
      }).run
      resolveReiniciarContrasenaFuture(validarClienteFuture, futureConsultaUsuarios, currentSender, message)

    case message: ConsultaUsuarioMessage =>
      val currentSender = sender
      if (message.token.isDefined)
        co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter.obtenerUsuarioToken(message.token.get) onComplete {
          case sFailure(failure) =>
            currentSender ! failure
          case sSuccess(value) => value match {
            case zSuccess(response) => currentSender ! response
            case zFailure(error) => currentSender ! error
          }
        }
      else
        currentSender ! None

    case message: UsuarioAceptaHabeasDataMessage =>
      val currentSender = sender
      val usuarioHabeas: UsuarioAceptaHabeasDataMessage = message
      enviarCorreoHabeasData(usuarioHabeas.perfilCliente, usuarioHabeas.identificacion, usuarioHabeas.tipoIdentificacion,
        usuarioHabeas.correoCliente, currentSender, usuarioHabeas.idUsuario, usuarioHabeas.habeasData, usuarioHabeas.idFormulario)
  }

  private def resolveDesbloquearContrasenaFuture(futureCliente: Future[Validation[ErrorValidacion, (Option[Usuario], Cliente)]], currentSender: ActorRef,
    message: DesbloquearMessage) = {
    futureCliente onComplete {
      case sFailure(failure) =>
        currentSender ! failure
      case sSuccess(value) =>
        value match {
          case zSuccess(response: (Option[Usuario], Cliente)) =>
            val usuario: Usuario = response._1.get
            val cliente: Cliente = response._2
            enviarCorreoDefinirContrasena(1, usuario.identificacion, usuario.tipoIdentificacion, cliente.wcli_dir_correo, currentSender, usuario.id, true)
          case zFailure(error) =>
            error match {
              case errorPersistence: PersistenceException => currentSender ! errorPersistence.message
              case errorVal: ErrorValidacion => currentSender ! ResponseMessage(Conflict, errorVal.msg)
              case _ => unhandled((): Unit)
            }
        }
    }
  }

  private def resolveReiniciarContrasenaFuture(
    validarClienteFuture: Future[Validation[ErrorValidacion, Any]],
    validarUsuarioFuture: Future[Validation[PersistenceException, Option[Any]]],
    currentSender: ActorRef, message: OlvidoContrasenaMessage
  ) = {
    validarUsuarioFuture onComplete {
      case sFailure(failure) => currentSender ! failure
      case sSuccess(value) =>
        value match {
          case zSuccess(response: Option[Any]) =>
            response match {
              case Some(valueResponse) =>
                validarClienteFuture onComplete {
                  case sFailure(failure) =>
                    currentSender ! failure
                  case sSuccess(value) =>
                    value match {
                      case zSuccess(responseCliente: Cliente) =>
                        response.get match {

                          case valueResponseUsuarioEmpresarial: UsuarioEmpresarialAdmin =>
                            val empresaValidacionFuture = (for {
                              empresa <- ValidationT(esEmpresaActiva(valueResponseUsuarioEmpresarial.identificacion))
                            } yield {
                              empresa
                            }).run
                            empresaValidacionFuture.onComplete {
                              case sFailure(ex) => currentSender ! ex
                              case sSuccess(resp) =>
                                resp match {
                                  case zFailure(errorValidacion) => currentSender ! ResponseMessage(Conflict, errorEstadoEmpresa)
                                  case zSuccess(_) =>
                                    //El olvido de contrasena queda para usuarios en estado diferente a bloqueado por super admin
                                    // y pendiente activacion
                                    if (valueResponseUsuarioEmpresarial.estado != EstadosEmpresaEnum.bloqueadoPorAdmin.id &&
                                      valueResponseUsuarioEmpresarial.estado != EstadosEmpresaEnum.pendienteActivacion.id &&
                                      valueResponseUsuarioEmpresarial.estado != EstadosEmpresaEnum.pendienteReiniciarContrasena.id) {
                                      //Se cambia a estado reinicio de contraseña cuando el cliente hace click en el enlace del correo
                                      enviarCorreoDefinirContrasena(message.perfilCliente, message.identificacion, message.tipoIdentificacion,
                                        responseCliente.wcli_dir_correo, currentSender, Some(valueResponseUsuarioEmpresarial.id))
                                    } else
                                      currentSender ! ResponseMessage(Conflict, errorEstadoUsuarioNoPermitido)
                                }
                            }

                          case valueResponse: Usuario =>
                            //El olvido de contrasena queda para usuarios en estado diferente a pendiente de activacion
                            if (valueResponse.estado != EstadosUsuarioEnum.pendienteActivacion.id &&
                              valueResponse.estado != EstadosUsuarioEnum.pendienteReinicio.id)
                              enviarCorreoDefinirContrasena(message.perfilCliente, message.identificacion, message.tipoIdentificacion,
                                responseCliente.wcli_dir_correo, currentSender, valueResponse.id)
                            else
                              currentSender ! ResponseMessage(Conflict, errorEstadoUsuarioNoPermitido)

                          case agenteInmobiliario: UsuarioAgenteInmobiliario =>
                            olvidoContrasenaAgenteInmobiliario(currentSender, agenteInmobiliario)

                          case _ => log.info("Error al obtener usuario para olvido de contrasena")
                        }
                      case zFailure(error) =>
                        error match {
                          case errorPersistence: ErrorPersistence => currentSender ! errorPersistence.exception
                          case errorVal: ErrorValidacion =>
                            currentSender ! ResponseMessage(Conflict, errorVal.msg)
                        }
                      case _ => log.info("Error al obtener usuario para olvido de contrasena")
                    }
                }
              case None => currentSender ! ResponseMessage(Conflict, errorUsuarioNoExistePerfilClienteNiEmpresaAdmin)
            }
          case zFailure(error) => currentSender ! error
        }
    }
  }

  private def esEmpresaActiva(nit: String): Future[Validation[ErrorValidacion, Empresa]] = {
    log.info("Validando el estado de la empresa")
    val future: Future[Validation[PersistenceException, Option[Empresa]]] = DataAccessAdapterUsuario.obtenerEmpresaPorNit(nit)
    future.map(
      _.leftMap(pe => ErrorPersistence(pe.message, pe)).flatMap {
        (x: Option[Empresa]) =>
          x match {
            case Some(empresa) =>
              if (empresa.estado == EstadosDeEmpresaEnum.activa.id) zSuccess(empresa)
              else zFailure(ErrorEstadoInvalidoEmpresa(errorEstadoEmpresa))
            case None => zFailure(ErrorEmpresaNoExiste(errorEmpresaNoExiste))
          }
      }
    )
  }

  private def enviarCorreoDefinirContrasena(perfilCliente: Int, identificacion: String, tipoIdentificacion: Int, correoCliente: String, currentSender: ActorRef,
    idUsuario: Option[Int], desbloqueo: Boolean = false) = {

    val validacionConsulta = validacionConsultaTiempoExpiracion()

    validacionConsulta onComplete {
      case sFailure(failure) => currentSender ! failure
      case sSuccess(value) =>
        value match {
          case zSuccess(responseConf: Configuracion) =>

            val fechaActual: Calendar = Calendar.getInstance()
            fechaActual.add(Calendar.HOUR_OF_DAY, responseConf.valor.toInt)
            val tokenPin: PinData = TokenPin.obtenerToken(fechaActual.getTime)

            val pin = perfilCliente match {
              case 1 => PinUsuario(None, idUsuario.get, tokenPin.token, new Timestamp(tokenPin.fechaExpiracion.getTime), tokenPin.tokenHash.get)
              case 2 => PinAdmin(None, idUsuario.get, tokenPin.token, new Timestamp(tokenPin.fechaExpiracion.getTime), tokenPin.tokenHash.get)
              case _ => unhandled((): Unit)
            }

            val resultCrearPinUsuario: Future[Validation[PersistenceException, Int]] = pin match {
              case pinUsuario @ PinUsuario(param1, param2, param3, param4, param5) =>
                DataAccessAdapterUsuario.crearUsuarioPin(pinUsuario)
              case pinAdmin @ PinAdmin(param1, param2, param3, param4, param5) =>
                DataAccessAdapterUsuario.crearUsuarioClienteAdministradorPin(pinAdmin)
              case _ => Future.successful(Validation.failure(PersistenceException(new Exception, BusinessLevel,
                "Error ... por verifique los datos y vuelva a intentarlo")))
            }

            val asunto: String = if (desbloqueo) "alianza.smtp.asunto.desbloquearUsuario" else "alianza.smtp.asunto.reiniciarContrasena"
            val template: String = if (desbloqueo) "alianza.smtp.templatepin.desbloquearUsuario" else "alianza.smtp.templatepin.reiniciarContrasena"

            resultCrearPinUsuario onComplete {
              case sFailure(fail) => currentSender ! fail
              case sSuccess(valueResult) =>
                valueResult match {
                  case zFailure(fail) => currentSender ! fail
                  case zSuccess(intResult) =>
                    pin match {
                      case pinUsuarioDto @ PinUsuario(param1, param2, param3, param4, param5) =>
                        new SmtpServiceClient()(context.system).send(buildMessage(pinUsuarioDto, responseConf.valor.toInt, UsuarioMessage(
                          correoCliente,
                          identificacion, tipoIdentificacion, null, false, None
                        ), template, asunto), (_, _) => Unit)
                        currentSender ! ResponseMessage(Created)

                      case pinUsuarioEmpresarialAdminDto @ PinAdmin(param1, param2, param3, param4, param5) =>
                        new SmtpServiceClient()(context.system).send(buildMessage(pinUsuarioEmpresarialAdminDto, responseConf.valor.toInt,
                          UsuarioMessage(correoCliente, identificacion, tipoIdentificacion, null, false, None), template, asunto), (_, _) => Unit)
                        currentSender ! ResponseMessage(Created)
                    }
                }
            }
          case zFailure(error) =>
            error match {
              case errorPersistence: ErrorPersistence => currentSender ! errorPersistence.exception
              case errorFail => currentSender ! ResponseMessage(Conflict, errorFail.msg)
            }
        }
    }
  }

  private def obtenerCuestionario(sender: ActorRef, message: UsuarioMessage) = {
    val currentSender = sender
    val locator: ConfrontaUltraWebServiceServiceLocator =
      new ConfrontaUltraWebServiceServiceLocator(config.getString("confronta.service.obtenerCuestionario.location"))
    val stub: ConfrontaUltraWSSoapBindingStub = locator.getConfrontaUltraWS.asInstanceOf[ConfrontaUltraWSSoapBindingStub]
    val parametros: ParametrosSeguridadULTRADTO = new ParametrosSeguridadULTRADTO
    val parametrosUltra: ParametrosULTRADTO = new ParametrosULTRADTO
    parametros.setClaveCIFIN(config.getString("confronta.service.claveCIFIN"))
    parametros.setPassword(config.getString("confronta.service.password"))

    parametrosUltra.setCodigoDepartamento(config.getInt("confronta.service.departamento"))
    parametrosUltra.setCodigoCuestionario(config.getInt("confronta.service.cuestionario"))
    parametrosUltra.setTelefono("")
    parametrosUltra.setCodigoCiudad(config.getInt("confronta.service.ciudad"))
    parametrosUltra.setPrimerApellido(message.primerApellido.get.toUpperCase())
    parametrosUltra.setCodigoTipoIdentificacion(if (message.tipoIdentificacion.toString.equals("1")) { "1" } else { "3" })
    parametrosUltra.setNumeroIdentificacion(message.identificacion)
    parametrosUltra.setFechaExpedicion(message.fechaExpedicion.get)

    val response: CuestionarioULTRADTO = stub.obtenerCuestionario(parametros, parametrosUltra)
    if (response.getRespuestaProceso.getCodigoRespuesta == 1) {
      currentSender ! JsonUtil.toJson(response)
    } else if (response.getRespuestaProceso.getCodigoRespuesta == 25) {
      currentSender ! ResponseMessage(Conflict, errorUsuarioExiste)
    } else {
      val respToSender = new ResultadoEvaluacionCuestionarioULTRADTO()
      respToSender.setRespuestaProceso(response.getRespuestaProceso)
      respToSender.getRespuestaProceso.setDescripcionRespuesta("No es posible realizar el registro, por favor llamar a la línea de atención.")
      currentSender ! respToSender.toJson
    }
  }

  private def validaSolicitudCliente(identificacion: String, tipoIdentificacion: Int): Future[Validation[ErrorValidacion, Cliente]] = {
    val consultaClienteFuture = validacionConsultaCliente(identificacion, tipoIdentificacion, true)
    (for {
      cliente <- ValidationT(consultaClienteFuture)
    } yield {
      cliente
    }).run
  }

  private def cambiarEstadoUsuario(numeroIdentificacion: String, estado: EstadosUsuarioEnum.estadoUsuario): Future[Validation[ErrorValidacion, Int]] = {
    DataAccessAdapterUsuario.actualizarEstadoUsuario(numeroIdentificacion, estado.id).map(_.leftMap(pe => ErrorPersistence(pe.message, pe)))
  }

  private def cambiarEstadoUsuarioEmpresarial(numeroIdentificacion: Int, estado: EstadosEmpresaEnum.estadoEmpresa): Future[Validation[ErrorValidacion, Int]] = {
    DataAccessAdapterUsuario.actualizarEstadoUsuarioEmpresarialAdmin(numeroIdentificacion, estado.id).map(_.leftMap(pe => ErrorPersistence(pe.message, pe)))
  }

  private def validaSolicitud(message: UsuarioMessage): Future[Validation[ErrorValidacion, Cliente]] = {
    val validacionClave: Future[Validation[ErrorValidacion, Unit.type]] = validacionReglasClaveAutoregistro(message)
    val consultaNumDocFuture = validacionConsultaNumDoc(message)
    val consultaClienteFuture: Future[Validation[ErrorValidacion, Cliente]] =
      validacionConsultaCliente(message.identificacion, message.tipoIdentificacion, false)
    (for {
      resultValidacionClave <- ValidationT(validacionClave)
      resultConsultaNumDoc <- ValidationT(consultaNumDocFuture)
      cliente <- ValidationT(consultaClienteFuture)
    } yield {
      cliente
    }).run
  }

  private def resolveCrearUsuarioFuture(crearUsuarioFuture: Future[Validation[ErrorValidacion, Cliente]], currentSender: ActorRef, message: UsuarioMessage) = {
    crearUsuarioFuture onComplete {
      case sFailure(failure) =>
        currentSender ! failure
      case sSuccess(value) =>
        value match {
          case zSuccess(response) =>
            obtenerCuestionario(currentSender, message)
          case zFailure(error) =>
            error match {
              case errorPersistence: ErrorPersistence => currentSender ! errorPersistence.exception
              case errorVal: ErrorValidacion => currentSender ! ResponseMessage(Conflict, errorVal.msg)
              case _ => unhandled((): Unit)
            }
        }
    }
  }

  private def buildMessage(pinUsuario: Any, numHorasCaducidad: Int, message: UsuarioMessage, templateBody: String, asuntoTemp: String) = {
    val body: String = pinUsuario match {
      case pinUsuarioDto @ PinUsuario(param1, param2, param3, param4, param5) =>
        new MailMessageUsuario(templateBody).getMessagePin(pinUsuarioDto, numHorasCaducidad, "1", "1")
      case pinUsuarioEmpresarialAdminDto @ PinAdmin(param1, param2, param3, param4, param5) =>
        new MailMessageUsuario(templateBody).getMessagePin(pinUsuarioEmpresarialAdminDto, numHorasCaducidad, "2", "1")
      case pinUsuarioEmpresarialAdminDto @ PinAgente(param1, param2, param3, param4, param5, param6) =>
        new MailMessageUsuario(templateBody).getMessagePin(pinUsuarioEmpresarialAdminDto, numHorasCaducidad, "2", "5")
    }
    val asunto: String = config.getString(asuntoTemp)
    MailMessage(config.getString("alianza.smtp.from"), message.correo, List(), asunto, body, "")
  }

  private def buildMessagePin(pinUsuario: Any, numHorasCaducidad: Int, message: UsuarioMessage, templateBody: String, asuntoTemp: String, idFormulario: String) = {
    val body: String = pinUsuario match {
      case pinUsuarioDto @ PinUsuario(param1, param2, param3, param4, param5) =>
        new MailMessageUsuario(templateBody).getMessagePin(pinUsuarioDto, numHorasCaducidad, "1", "1", idFormulario)
      case pinUsuarioEmpresarialAdminDto @ PinAgente(param1, param2, param3, param4, param5, param6) =>
        new MailMessageUsuario(templateBody).getMessagePin(pinUsuarioEmpresarialAdminDto, numHorasCaducidad, "2", "5", idFormulario)
    }
    val asunto: String = config.getString(asuntoTemp)
    MailMessage(config.getString("alianza.smtp.from"), message.correo, List(), asunto, body, "")
  }

  private val errorEstadoReinicioContrasena =
    ErrorMessage("409.8", "El usuario se encuentra en proceso de reinicio de contrasena", "El usuario se encuentra en proceso de reinicio de contrasena").toJson
  private val errorEstadoUsuarioNoPermitido =
    ErrorMessage("409.9", "El estado del usuario no permite reiniciar la contrasena", "El estado del usuario no permite reiniciar la contrasena").toJson
  private val errorUsuarioExiste = ErrorMessage("409.10", "Fecha de Expedición Invalida", "Fecha de Expedición Invalida").toJson
  private val errorEstadoEmpresa = ErrorMessage("409.11", "Estado no valido de la empresa", "Estado no valido de la empresa").toJson
  private val errorEmpresaNoExiste = ErrorMessage("409.12", "Empresa no existe para dicho NÚMERO", "Empresa no existe para dicho NÚMERO").toJson
  private val errorUsuarioNoExistePerfilClienteNiEmpresaAdmin =
    ErrorMessage("409.13", "Usuario no existe para perfil cliente, cliente admin", "Usuario no existe para perfil cliente, cliente admin").toJson
  private val errorUsuarioEmpresaAdminActivo = ErrorMessage("409.14", "Usuario admin ya existe", "Ya hay un cliente administrador para ese NIT.").toJson

  // --------------------------------------
  // Extensión portal alianza inmobiliaria
  // --------------------------------------

  def buscarAgenteInmobiliario(busquedaUsuarioPortal: Validation[PersistenceException, Option[Any]], identificacion: String, usuario: String): Future[Validation[PersistenceException, Option[Any]]] = {

    busquedaUsuarioPortal match {
      case zFailure(failure) => Future.successful(Validation.failure(failure))
      case zSuccess(usuarioOpt) => usuarioOpt match {
        case Some(us) => Future.successful(Validation.success(Some(us)))
        case None =>
          // validar si es agente inmobiliario
          agentesInmobDao.get(identificacion, usuario).map { agenteInmobOpt =>
            Validation.success(agenteInmobOpt)
          }
      }
    }
  }

  def olvidoContrasenaAgenteInmobiliario(
    currentSender: ActorRef,
    agente: UsuarioAgenteInmobiliario
  ): Unit = {
    esEmpresaActiva(agente.identificacion).flatMap {
      case zFailure(failure) => Future.successful(ResponseMessage(Conflict, errorEstadoEmpresa))
      case zSuccess(_) =>
        if (agente.estado != EstadosUsuarioEnumInmobiliario.inactivo.id) {
          for {
            configExpiracion <- configDao.getByKey(ConfiguracionEnum.EXPIRACION_PIN.name)
            pinAgente: PinAgenteInmobiliario = agentesInmobPinRepo.generarPinAgente(configExpiracion, agente.id, reinicio = true)
            idPin <- agentesInmobPinRepo.asociarPinAgente(pinAgente)
            correoReinicio: MailMessage = agentesInmobPinRepo.generarCorreoReinicio(
              pinAgente.tokenHash, configExpiracion.valor.toInt,
              //agente.nombre.getOrElse(agente.usuario),
              agente.usuario,
              agente.correo
            )
          } yield {
            agentesInmobPinRepo.enviarEmail(correoReinicio)(context.system)
            ResponseMessage(Created)
          }
        } else {
          Future.successful(ResponseMessage(Conflict, errorEstadoUsuarioNoPermitido))
        }
    }.onComplete {
      case sFailure(exception) => currentSender ! exception
      case sSuccess(responseMessage) => currentSender ! responseMessage
    }
  }

  private def enviarCorreoHabeasData(perfilCliente: Int, identificacion: String, tipoIdentificacion: Int, correoCliente: String, currentSender: ActorRef,
    idUsuario: Option[Int], habeasData: Boolean = false, idFormaulario: String) = {

    val validacionConsulta = validacionConsultaTiempoExpiracion()

    validacionConsulta onComplete {
      case sFailure(failure) => currentSender ! failure
      case sSuccess(value) =>
        value match {
          case zSuccess(responseConf: Configuracion) =>

            val fechaActual: Calendar = Calendar.getInstance()
            fechaActual.add(Calendar.HOUR_OF_DAY, responseConf.valor.toInt)
            val tokenPin: PinData = TokenPin.obtenerToken(fechaActual.getTime)

            val pin = perfilCliente match {
              case 1 => PinUsuario(None, idUsuario.get, tokenPin.token, new Timestamp(tokenPin.fechaExpiracion.getTime), tokenPin.tokenHash.get)
              case 2 => PinAgente(None, idUsuario.get, tokenPin.token, new Timestamp(tokenPin.fechaExpiracion.getTime), tokenPin.tokenHash.get, 99)
              case _ => unhandled((): Unit)
            }

            val resultCrearPinUsuario: Future[Validation[PersistenceException, Int]] = pin match {
              case pinUsuario @ PinUsuario(param1, param2, param3, param4, param5) =>
                DataAccessAdapterUsuario.crearUsuarioPin(pinUsuario)
              case pinAdmin @ PinAdmin(param1, param2, param3, param4, param5) =>
                DataAccessAdapterUsuario.crearUsuarioClienteAdministradorPin(pinAdmin)
              case _ => Future.successful(Validation.failure(PersistenceException(new Exception, BusinessLevel,
                "Error ... por verifique los datos y vuelva a intentarlo")))
            }

            val asunto: String = if (habeasData) "alianza.smtp.asunto.habeasdata" else "alianza.smtp.asunto.reiniciarContrasena"
            val template: String = if (habeasData) "alianza.smtp.templatepin.aceptacionHabeasDataTerceros" else "alianza.smtp.templatepin.reiniciarContrasena"

            resultCrearPinUsuario onComplete {
              case sFailure(fail) => currentSender ! fail
              case sSuccess(valueResult) =>
                valueResult match {
                  case zFailure(fail) => currentSender ! fail
                  case zSuccess(intResult) =>
                    pin match {
                      case pinUsuarioDto @ PinUsuario(param1, param2, param3, param4, param5) =>
                        new SmtpServiceClient()(context.system).send(buildMessagePin(pinUsuarioDto, responseConf.valor.toInt, UsuarioMessage(
                          correoCliente,
                          identificacion, tipoIdentificacion, null, false, None
                        ), template, asunto, idFormaulario), (_, _) => Unit)
                        currentSender ! ResponseMessage(Created)

                      case pinUsuarioEmpresarialAdminDto @ PinAgente(param1, param2, param3, param4, param5, parm6) =>
                        new SmtpServiceClient()(context.system).send(buildMessagePin(pinUsuarioEmpresarialAdminDto, responseConf.valor.toInt,
                          UsuarioMessage(correoCliente, identificacion, tipoIdentificacion, null, false, None), template, asunto, idFormaulario), (_, _) => Unit)
                        currentSender ! ResponseMessage(Created)
                    }
                }
            }
          case zFailure(error) =>
            error match {
              case errorPersistence: ErrorPersistence => currentSender ! errorPersistence.exception
              case errorFail => currentSender ! ResponseMessage(Conflict, errorFail.msg)
            }
        }
    }
  }

}

