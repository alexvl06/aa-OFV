package co.com.alianza.domain.aggregates.usuarios

import akka.actor.{ActorRef, Actor, ActorLogging}
import scalaz.{Failure => zFailure, Success => zSuccess, Validation}
import scala.util.{Failure => sFailure, Success => sSuccess}
import co.com.alianza.app.{MainActors, AlianzaActors}
import co.com.alianza.util.json.MarshallableImplicits._
import spray.http.StatusCodes._
import scala.concurrent.Future
import co.com.alianza.infrastructure.anticorruption.usuarios.{DataAccessAdapter => DataAccessAdapterUsuario}
import co.com.alianza.infrastructure.anticorruption.pin.{ DataAccessTranslator => DataAccessTranslatorPin }
import co.com.alianza.infrastructure.anticorruption.pinclienteadmin.{ DataAccessTranslator => DataAccessTranslatorPinClienteAdmin }
import co.com.alianza.util.clave.Crypto
import com.typesafe.config.Config
import enumerations.{EstadosEmpresaEnum, EstadosUsuarioEnum, AppendPasswordUser}

import akka.actor.Props
import co.com.alianza.util.token.{TokenPin, PinData}
import akka.routing.RoundRobinPool
import scalaz.Failure
import scala.util.{Success, Failure}
import co.com.alianza.infrastructure.messages._
import co.com.alianza.infrastructure.dto._
import scalaz.Success
import co.com.alianza.util.transformers.ValidationT
import co.com.alianza.persistence.entities
import co.com.alianza.microservices.{MailMessage, SmtpServiceClient}
import co.com.alianza.domain.aggregates.usuarios.MailMessageUsuario
import scalaz.Failure
import scalaz.Success
import scala.util.Success
import java.util.{Calendar, Date}
import scalaz.std.AllInstances._
import co.com.alianza.util.FutureResponse
import scala.Some
import co.com.alianza.infrastructure.messages.OlvidoContrasenaMessage
import co.com.alianza.util.transformers.ValidationT
import co.com.alianza.microservices.MailMessage
import akka.routing.RoundRobinPool
import co.com.alianza.util.token.PinData
import co.com.alianza.infrastructure.messages.UsuarioMessage
import co.com.alianza.infrastructure.messages.ResponseMessage
import com.asobancaria.cifinpruebas.cifin.confrontav2plusws.services.ConfrontaUltraWS.{ConfrontaUltraWSSoapBindingStub, ConfrontaUltraWebServiceServiceLocator}
import co.cifin.confrontaultra.dto.ultra.{ResultadoEvaluacionCuestionarioULTRADTO, CuestionarioULTRADTO, ParametrosULTRADTO, ParametrosSeguridadULTRADTO}
import co.com.alianza.util.json.JsonUtil
import co.com.alianza.exceptions.{BusinessLevel, PersistenceException}
import co.com.alianza.domain.aggregates.autenticacion.errores.{ErrorCredencialesInvalidas, ErrorPersistencia, ErrorAutenticacion}
import co.com.alianza.infrastructure.dto.Empresa
import enumerations.empresa.EstadosDeEmpresaEnum


class UsuariosActorSupervisor extends Actor with ActorLogging {
  import akka.actor.SupervisorStrategy._
  import akka.actor.OneForOneStrategy

  val usuariosActor = context.actorOf(Props[UsuariosActor].withRouter(RoundRobinPool(nrOfInstances = 2)), "usuariosActor")
  val usuarioEmpresarialActor = context.actorOf(Props[UsuarioEmpresarialActor].withRouter(RoundRobinPool(nrOfInstances = 2)), "usuarioEmpresarialActor")

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

/**
 *
 */
class UsuariosActor extends Actor with ActorLogging with AlianzaActors {

  import scala.concurrent.ExecutionContext

  implicit val ex: ExecutionContext = MainActors.dataAccesEx
  implicit val sys = context.system
  implicit private val config: Config = MainActors.conf

  import ValidacionesUsuario._

  def receive = {

    case message:UsuarioMessage  =>

      val currentSender = sender()

      val crearUsuarioFuture = (for{
        captchaVal <-  ValidationT(validaCaptcha(message))
        cliente <- ValidationT(validaSolicitud(message))
      }yield{
        cliente
      }).run

      resolveCrearUsuarioFuture(crearUsuarioFuture,currentSender,message)

    case message: DesbloquarMessage =>
      val currentSender = sender()
      val toUsuarioMessageAux: UsuarioMessage = message.toUsuarioMessage

      val futureCliente = (for {
        captchaVal <- ValidationT(validaCaptcha(toUsuarioMessageAux))
        cliente <- ValidationT(validacionUsuarioNumDoc(toUsuarioMessageAux))
      } yield {
        cliente
      }).run

      resolveDesbloquearContrasenaFuture(futureCliente, currentSender, message)

    case message: OlvidoContrasenaMessage =>

      val currentSender = sender()


      //Se obtiene el usuario dado el perfil que llegue de presentacion, en caso de perfil no correcto se devuelve excepcion
      val futureConsultaUsuarios: Future[Validation[PersistenceException, Option[Any]]] = message.perfilCliente match {
        case 1 => co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter.obtenerUsuarioNumeroIdentificacion(message.identificacion)
        case 2 => co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter.obtieneUsuarioEmpresarialAdminPorNitYUsuario(message.identificacion, message.usuarioClienteAdmin.get, message.tipoIdentificacion)
        case _ => Future.successful(Validation.failure(PersistenceException(new Exception, BusinessLevel, "El perfil del usuario no es soportado por la aplicacion")))
      }

      val validarClienteFuture = ( for {
        cliente <- ValidationT(validaSolicitudCliente(message))
      } yield {
        cliente
      }).run


      resolveReiniciarContrasenaFuture(validarClienteFuture, futureConsultaUsuarios, currentSender, message)

    case message: ConsultaUsuarioMessage =>
      val currentSender = sender
      if(message.token.isDefined)
        co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter.obtenerUsuarioToken(message.token.get) onComplete {
          case sFailure( failure ) =>
            currentSender ! failure
          case sSuccess (value) => value match {
            case zSuccess (response) => currentSender ! response
            case zFailure( error ) => currentSender ! error
          }
        }
      else
        currentSender ! None

  }

  private def resolveReiniciarContrasenaFuture( validarClienteFuture: Future[Validation[ErrorValidacion, Any]],
                                                validarUsuarioFuture: Future[Validation[PersistenceException, Option[Any]]],
                                                currentSender: ActorRef, message: OlvidoContrasenaMessage) = {
    validarUsuarioFuture onComplete {
      case sFailure(failure) => currentSender ! failure
      case sSuccess(value) =>
        value match {
          case zSuccess(response: Option[Any]) =>
            response match {
              case Some(valueResponse) =>
                validarClienteFuture onComplete{
                  case sFailure( failure ) =>
                    currentSender ! failure
                  case sSuccess (value) =>
                    value match{
                      case zSuccess( responseCliente:Cliente ) =>
                        response.get match {
                          case valueResponseUsuarioEmpresarial:UsuarioEmpresarialAdmin =>

                            val empresaValidacionFuture = (for {
                              empresa <- ValidationT(esEmpresaActiva(valueResponseUsuarioEmpresarial.identificacion))
                            } yield {
                              empresa
                            }).run

                            empresaValidacionFuture.onComplete {
                              case sFailure(ex) => currentSender ! ex
                              case sSuccess(resp) =>
                                resp match {
                                  case zFailure(errorValidacion) => currentSender ! ResponseMessage(Conflict,errorEstadoEmpresa)
                                  case zSuccess(_) =>
                                    //El olvido de contrasena queda para usuarios en estado activo, pendiente de activacion, pendiente de reinicio de contrasena
                                    if( valueResponseUsuarioEmpresarial.estado == EstadosEmpresaEnum.activo.id ||
                                      valueResponseUsuarioEmpresarial.estado == EstadosEmpresaEnum.pendienteActivacion.id ||
                                      valueResponseUsuarioEmpresarial.estado == EstadosEmpresaEnum.pendienteReiniciarContrasena.id  ) {

                                      //Se cambia a estado reinicio de contraseña cuando el cliente hace click en el enlace del correo
                                      enviarCorreoOlvidoContrasena(responseCliente.wcli_dir_correo, currentSender, message, Some(valueResponseUsuarioEmpresarial.id))
                                    }
                                    /*                            else if( valueResponseUsuarioEmpresarial.estado == EstadosEmpresaEnum.nuevoEstado.id )
                                                                  currentSender ! ResponseMessage(Conflict,errorEstadoReinicioContrasena)*/
                                    else
                                      currentSender ! ResponseMessage(Conflict,errorEstadoUsuarioNoPermitido)
                                }
                            }




                          case valueResponse:Usuario =>

                            //El olvido de contrasena queda para usuarios en estado bloqueado por contrasena y activos
                            if( valueResponse.estado == EstadosUsuarioEnum.activo.id || valueResponse.estado == EstadosUsuarioEnum.bloqueContraseña.id  ) {

                              /*val actualizarContrasenaFuture = (for {
                                idUsuario <- ValidationT(cambiarEstadoUsuario(message.identificacion, EstadosUsuarioEnum.pendienteReinicio))
                              } yield {
                                idUsuario
                              }).run*/

                              enviarCorreoOlvidoContrasena(responseCliente.wcli_dir_correo, currentSender, message, valueResponse.id)
                            }
                            else if( valueResponse.estado == EstadosUsuarioEnum.pendienteReinicio.id )
                              currentSender ! ResponseMessage(Conflict,errorEstadoReinicioContrasena)
                            else
                              currentSender ! ResponseMessage(Conflict,errorEstadoUsuarioNoPermitido)



                          case _ => log.info("Error al obtener usuario para olvido de contrasena")
                        }
                      case zFailure(error) =>
                        error match {
                          case errorPersistence: ErrorPersistence => currentSender ! errorPersistence.exception
                          case errorVal: ErrorValidacion =>
                            currentSender ! ResponseMessage(Conflict, errorVal.msg)
                          case errorClienteNoExiste:ErrorClienteNoExiste  => currentSender ! errorClienteNoExiste
                        }
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
    val future : Future[Validation[PersistenceException, Option[Empresa]]] = DataAccessAdapterUsuario.obtenerEmpresaPorNit(nit)
    future.map(
      _.leftMap(pe => ErrorPersistence(pe.message, pe)).flatMap{
        (x:Option[Empresa]) => x match {
          case Some(empresa) =>
            if (empresa.estado == EstadosDeEmpresaEnum.activa.id) zSuccess(empresa)
            else zFailure(ErrorEstadoInvalidoEmpresa(errorEstadoEmpresa))
          case None => zFailure(ErrorEmpresaNoExiste(errorEmpresaNoExiste))
        }
      }
    )
  }

  //actualizarContrasenaFuture: Future[Validation[ErrorValidacion, Int]]
  private def enviarCorreoOlvidoContrasena( correoCliente:String,  currentSender: ActorRef, message: OlvidoContrasenaMessage, idUsuario:Option[Int] ) = {



              val validacionConsulta = validacionConsultaTiempoExpiracion()

              validacionConsulta onComplete {
                case sFailure(failure) => currentSender ! failure
                case sSuccess(value) =>
                  value match {
                    case zSuccess(responseConf: Configuracion) =>

                      val fechaActual: Calendar = Calendar.getInstance()
                      fechaActual.add(Calendar.HOUR_OF_DAY, responseConf.valor.toInt)
                      val tokenPin: PinData = TokenPin.obtenerToken(fechaActual.getTime)

                      val pin = message.perfilCliente match {
                        case 1 => PinUsuario(None, idUsuario.get, tokenPin.token, tokenPin.fechaExpiracion, tokenPin.tokenHash.get)
                        case 2 => PinUsuarioEmpresarialAdmin(None, idUsuario.get, tokenPin.token, tokenPin.fechaExpiracion, tokenPin.tokenHash.get)
                      }

                      val resultCrearPinUsuario: Future[Validation[PersistenceException, Int]] = pin match {
                        case pinUsuarioDto @ PinUsuario(param1, param2, param3, param4, param5) =>
                          val puPersistence: entities.PinUsuario = DataAccessTranslatorPin.translateEntityPinUsuario(pinUsuarioDto)
                          DataAccessAdapterUsuario.crearUsuarioPin(puPersistence)
                        case pinUsuarioEmpresarialAdminDto @ PinUsuarioEmpresarialAdmin(param1, param2, param3, param4, param5) =>
                          val pueaPersistence: entities.PinUsuarioEmpresarialAdmin = DataAccessTranslatorPinClienteAdmin.translateEntityPinUsuario(pinUsuarioEmpresarialAdminDto)
                          DataAccessAdapterUsuario.crearUsuarioClienteAdministradorPin(pueaPersistence)
                        case _ => Future.successful(Validation.failure(PersistenceException(new Exception, BusinessLevel, "Error ... por verifique los datos y vuelva a intentarlo")))
                      }

                      resultCrearPinUsuario onComplete {
                        case sFailure(fail) => currentSender ! fail
                        case sSuccess(valueResult) =>
                          valueResult match {
                            case zFailure(fail) => currentSender ! fail
                            case zSuccess(intResult) =>
                              pin match {
                                case pinUsuarioDto @ PinUsuario(param1, param2, param3, param4, param5) =>
                                  new SmtpServiceClient().send(buildMessage(pinUsuarioDto, responseConf.valor.toInt, UsuarioMessage(correoCliente, message.identificacion, message.tipoIdentificacion,null, false, None), "alianza.smtp.templatepin.reiniciarContrasena", "alianza.smtp.asunto.reiniciarContrasena"), (_, _) => Unit)
                                  currentSender ! ResponseMessage(Created)

                                case pinUsuarioEmpresarialAdminDto @ PinUsuarioEmpresarialAdmin(param1, param2, param3, param4, param5) =>
                                  new SmtpServiceClient().send(buildMessage(pinUsuarioEmpresarialAdminDto, responseConf.valor.toInt, UsuarioMessage(correoCliente, message.identificacion, message.tipoIdentificacion,null, false, None), "alianza.smtp.templatepin.reiniciarContrasena", "alianza.smtp.asunto.reiniciarContrasena"), (_, _) => Unit)
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

  private def obtenerCuestionario(sender:ActorRef, message:UsuarioMessage) = {
    val currentSender = sender

    val locator: ConfrontaUltraWebServiceServiceLocator = new ConfrontaUltraWebServiceServiceLocator(config.getString("confronta.service.obtenerCuestionario.location"))
    val stub: ConfrontaUltraWSSoapBindingStub = locator.getConfrontaUltraWS.asInstanceOf[ConfrontaUltraWSSoapBindingStub]
    val parametros: ParametrosSeguridadULTRADTO = new ParametrosSeguridadULTRADTO
    parametros.setClaveCIFIN(config.getString("confronta.service.claveCIFIN"))
    parametros.setPassword(config.getString("confronta.service.password"))
    val parametrosUltra: ParametrosULTRADTO = new ParametrosULTRADTO
    parametrosUltra.setCodigoDepartamento(config.getInt("confronta.service.departamento"))
    parametrosUltra.setCodigoCuestionario(config.getInt("confronta.service.cuestionario"))
    parametrosUltra.setTelefono("");
    parametrosUltra.setCodigoCiudad(config.getInt("confronta.service.ciudad"))
    parametrosUltra.setPrimerApellido(message.primerApellido.get.toUpperCase())
    parametrosUltra.setCodigoTipoIdentificacion(if(message.tipoIdentificacion.toString.equals("1")){"1"}else{"3"})
    parametrosUltra.setNumeroIdentificacion(message.identificacion)
    parametrosUltra.setFechaExpedicion(message.fechaExpedicion.get)

    val response: CuestionarioULTRADTO = stub.obtenerCuestionario(parametros, parametrosUltra)
    if(response.getRespuestaProceso.getCodigoRespuesta == 1){
      currentSender ! JsonUtil.toJson(response)
    }else if(response.getRespuestaProceso.getCodigoRespuesta == 25){
      currentSender !  ResponseMessage(Conflict, errorUsuarioExiste)
    }else {
      val respToSender = new ResultadoEvaluacionCuestionarioULTRADTO()
      respToSender.setRespuestaProceso(response.getRespuestaProceso)
      respToSender.getRespuestaProceso.setDescripcionRespuesta("No es posible realizar el registro, por favor llamar a la línea de atención.")
      currentSender !  respToSender.toJson
    }

  }

  private def validaSolicitudCliente(message: OlvidoContrasenaMessage): Future[Validation[ErrorValidacion, Cliente]] = {

    val consultaClienteFuture = validacionConsultaCliente(UsuarioMessage("",message.identificacion,message.tipoIdentificacion, null, false, None))

    (for {
      cliente <- ValidationT(consultaClienteFuture)
    } yield {
      cliente
    }).run

  }

  private def cambiarEstadoUsuario(numeroIdentificacion: String, estado: EstadosUsuarioEnum.estadoUsuario): Future[Validation[ErrorValidacion, Int]] = {
    DataAccessAdapterUsuario.actualizarEstadoUsuario(numeroIdentificacion, estado.id).map(_.leftMap(pe => ErrorPersistence(pe.message, pe)));
  }

  private def cambiarEstadoUsuarioEmpresarial(numeroIdentificacion: Int, estado: EstadosEmpresaEnum.estadoEmpresa): Future[Validation[ErrorValidacion, Int]] = {
    DataAccessAdapterUsuario.actualizarEstadoUsuarioEmpresarialAdmin(numeroIdentificacion, estado.id).map(_.leftMap(pe => ErrorPersistence(pe.message, pe)));
  }


  private def validaSolicitud(message:UsuarioMessage): Future[Validation[ErrorValidacion, Cliente]] = {

    val consultaNumDocFuture = validacionConsultaNumDoc(message)
    //Se quita la validación ya que alianza quiere permitir registro de usuarios a la misma cuenta de correo.
    //val consultaCorreoFuture: Future[Validation[ErrorValidacion, Unit.type]] = validacionConsultaCorreo(message)
    val consultaClienteFuture: Future[Validation[ErrorValidacion, Cliente]] = validacionConsultaCliente(message)
    val validacionClave: Future[Validation[ErrorValidacion, Unit.type]] = validacionReglasClaveAutoregistro(message)

    (for{
      resultValidacionClave <- ValidationT(validacionClave)
      resultConsultaNumDoc <- ValidationT(consultaNumDocFuture)
      //resultConsultaCorreo <- ValidationT(consultaCorreoFuture)
      cliente <- ValidationT(consultaClienteFuture)
    }yield {
      cliente
    }).run
  }

  private def guardarUsuario(message:UsuarioMessage): Future[Validation[ErrorValidacion, Int]] = {
    val passwordUserWithAppend = message.contrasena.concat( AppendPasswordUser.appendUsuariosFiducia );
    DataAccessAdapterUsuario.crearUsuario(message.toEntityUsuario( Crypto.hashSha512(passwordUserWithAppend))).map(_.leftMap( pe => ErrorPersistence(pe.message,pe)))
  }

  private def resolveCrearUsuarioFuture(crearUsuarioFuture: Future[Validation[ErrorValidacion, Cliente]], currentSender: ActorRef,message:UsuarioMessage) = {
    crearUsuarioFuture onComplete {
      case sFailure(failure)  =>    currentSender ! failure
      case sSuccess(value)    =>
        value match {
          case zSuccess(response) =>
            obtenerCuestionario(currentSender,message)
          case zFailure(error)  =>
            error match {
              case errorPersistence:ErrorPersistence  => currentSender !  errorPersistence.exception
              case errorVal:ErrorValidacion =>
                currentSender !  ResponseMessage(Conflict, errorVal.msg)
            }
        }
    }
  }

  private def resolveDesbloquearContrasenaFuture(futureCliente: Future[Validation[ErrorValidacion, Option[Usuario]]], currentSender: ActorRef, message: DesbloquarMessage) = {

    futureCliente onComplete {
      case sFailure( failure ) =>
        currentSender ! failure
      case sSuccess (value) =>
        value match{
          case zSuccess( response ) =>
            val mensajeCuestionario = message.toUsuarioMessage.copy(tipoIdentificacion = response.get.tipoIdentificacion)
            obtenerCuestionario(currentSender,mensajeCuestionario)
          case zFailure (error) =>
            error match {
              case errorPersistence:ErrorPersistence  => currentSender !  errorPersistence.exception
              case errorVal:ErrorValidacion =>
                currentSender !  ResponseMessage(Conflict, errorVal.msg)
            }
        }
    }
  }


  private def buildMessage(pinUsuario: Any, numHorasCaducidad: Int, message: UsuarioMessage, templateBody: String, asuntoTemp: String) = {
    val body: String = pinUsuario match {
      case pinUsuarioDto @ PinUsuario(param1, param2, param3, param4, param5) =>
        new MailMessageUsuario(templateBody).getMessagePin(pinUsuarioDto, numHorasCaducidad, "1", "1")
      case pinUsuarioEmpresarialAdminDto @ PinUsuarioEmpresarialAdmin(param1, param2, param3, param4, param5) =>
        new MailMessageUsuario(templateBody).getMessagePin(pinUsuarioEmpresarialAdminDto, numHorasCaducidad, "2", "1")
    }
    val asunto: String = config.getString(asuntoTemp)
    MailMessage(config.getString("alianza.smtp.from"), "jonathanforero@seven4n.com",  List() , asunto, body, "")
    //MailMessage(config.getString("alianza.smtp.from"), "josegarcia@seven4n.com", List(), asunto, body, "")
    //MailMessage(config.getString("alianza.smtp.from"), message.correo, List(), asunto, body, "")
  }


  private val errorEstadoReinicioContrasena = ErrorMessage("409.8", "El usuario se encuentra en proceso de reinicio de contrasena", "El usuario se encuentra en proceso de reinicio de contrasena").toJson
  private val errorEstadoUsuarioNoPermitido = ErrorMessage("409.9", "El estado del usuario no permite reiniciar la contrasena", "El estado del usuario no permite reiniciar la contrasena").toJson

  private val errorUsuarioExiste = ErrorMessage("409.10", "Fecha de Expedición Invalida", "Fecha de Expedición Invalida").toJson
  private val errorEstadoEmpresa = ErrorMessage("409.11", "Estado no valido de la empresa", "Estado no valido de la empresa").toJson
  private val errorEmpresaNoExiste = ErrorMessage("409.12", "Empresa no existe para dicho NIT", "Empresa no existe para dicho NIT").toJson
  private val errorUsuarioNoExistePerfilClienteNiEmpresaAdmin = ErrorMessage("409.13", "Usuario no existe para perfil cliente, cliente admin", "Usuario no existe para perfil cliente, cliente admin").toJson

}
