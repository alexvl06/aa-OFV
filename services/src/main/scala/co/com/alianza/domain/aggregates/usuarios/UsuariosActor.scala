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
import co.com.alianza.util.clave.Crypto
import com.typesafe.config.Config
import enumerations.{EstadosUsuarioEnum, AppendPasswordUser}

import akka.actor.Props
import co.com.alianza.util.token.{TokenPin}
import co.com.alianza.infrastructure.messages._
import co.com.alianza.persistence.entities
import co.com.alianza.microservices.{SmtpServiceClient}
import co.com.alianza.infrastructure.dto.PinUsuario
import scala.Some
import co.com.alianza.infrastructure.messages.OlvidoContrasenaMessage
import co.com.alianza.util.transformers.ValidationT
import co.com.alianza.microservices.MailMessage
import akka.routing.RoundRobinPool
import co.com.alianza.util.token.PinData
import co.com.alianza.infrastructure.dto.Cliente
import co.com.alianza.infrastructure.dto.Usuario
import co.com.alianza.infrastructure.messages.UsuarioMessage
import co.com.alianza.infrastructure.messages.ResponseMessage
import com.asobancaria.cifinpruebas.cifin.confrontav2plusws.services.ConfrontaUltraWS.{ConfrontaUltraWSSoapBindingStub, ConfrontaUltraWebServiceServiceLocator}
import co.cifin.confrontaultra.dto.ultra.{CuestionarioULTRADTO, ParametrosULTRADTO, ParametrosSeguridadULTRADTO}
import co.com.alianza.util.json.JsonUtil


class UsuariosActorSupervisor extends Actor with ActorLogging {
  import akka.actor.SupervisorStrategy._
  import akka.actor.OneForOneStrategy

  val usuariosActor = context.actorOf(Props[UsuariosActor].withRouter(RoundRobinPool(nrOfInstances = 2)), "usuariosActor")

  def receive = {

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

  import ValdiacionesUsuario._

  def receive = {

    case message:UsuarioMessage  =>

      val currentSender = sender()

      val crearUsuarioFuture = (for{
        captchaVal <-  ValidationT(validaCaptcha(message))
        cliente <- ValidationT(validaSolicitud(message))
        //idUsuario <- ValidationT(guardarUsuario(message))
      }yield{
        cliente
      }).run

      resolveCrearUsuarioFuture(crearUsuarioFuture,currentSender,message)



    case message: OlvidoContrasenaMessage =>

      val currentSender = sender()


      val validarClienteFuture = ( for {
        cliente <- ValidationT(validaSolicitudCliente(message))
      } yield {
        cliente
      }).run

      resolveReiniciarContrasenaFuture(validarClienteFuture, currentSender, message)

  }



  private def resolveReiniciarContrasenaFuture( validarClienteFuture: Future[Validation[ErrorValidacion, Cliente]],  currentSender: ActorRef, message: OlvidoContrasenaMessage) = {
    validarClienteFuture onComplete{
      case sFailure( failure ) =>
        println(failure)
        currentSender ! failure
      case sSuccess (value) =>
        value match{
          case zSuccess( responseCliente:Cliente ) =>

            val resultUsuario = co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter.obtenerUsuarioNumeroIdentificacion(message.identificacion);

            resultUsuario onComplete {
              case sFailure(failure) => currentSender ! failure
              case sSuccess(value) =>
                value match {
                  case zSuccess(response: Option[Usuario]) =>
                    response match {
                      case Some(valueResponse) =>

                        //El olvido de contrasena queda para usuarios en estado bloqueado por contrasena y activos
                        if( valueResponse.estado == EstadosUsuarioEnum.activo.id || valueResponse.estado == EstadosUsuarioEnum.bloqueContraseña.id  ) {

                          val actualizarContrasenaFuture = (for {
                            idUsuario <- ValidationT(cambiarEstadoUsuario(message.identificacion, EstadosUsuarioEnum.pendienteReinicio))
                          } yield {
                            idUsuario
                          }).run

                          enviarCorreoOlvidoContrasena(actualizarContrasenaFuture, responseCliente.wcli_dir_correo, currentSender, message, valueResponse.id)
                        }
                        else if( valueResponse.estado == EstadosUsuarioEnum.pendienteReinicio.id )
                          currentSender ! ErrorEstadoUsuarioOlvidoContrasena(errorEstadoReinicioContrasena)
                        else
                          currentSender ! ErrorEstadoUsuarioOlvidoContrasena(errorEstadoUsuarioNoPermitido)

                      case None => currentSender ! ResponseMessage(Unauthorized, "Error al obtener usuario por numero de identificacion")
                    }
                  case zFailure(error) => currentSender ! error
                }
            }
          case zFailure(error) =>
            error match {
              case errorPersistence: ErrorPersistence => currentSender ! errorPersistence.exception
              case errorVal: ErrorValidacion =>
                currentSender ! ResponseMessage(Conflict, errorVal.msg)
              case errorClienteNoExiste:ErrorClienteNoExiste  => currentSender !errorClienteNoExiste
            }
        }
    }



  }


  private def enviarCorreoOlvidoContrasena( actualizarContrasenaFuture: Future[Validation[ErrorValidacion, Int]], correoCliente:String,  currentSender: ActorRef, message: OlvidoContrasenaMessage, idUsuario:Option[Int] ) = {

    actualizarContrasenaFuture onComplete {
      case sFailure(failure) =>
        println(failure)
        currentSender ! failure
      case sSuccess(value) =>
        value match {
          case zSuccess(response: Int) =>
            currentSender ! ResponseMessage(Created, response.toJson)
            if (response == 1) {

              val tokenPin: PinData = TokenPin.obtenerToken()
              val pin: PinUsuario = PinUsuario(None, idUsuario.get, tokenPin.token, tokenPin.fechaExpiracion, tokenPin.tokenHash.get)
              val pinUsuario: entities.PinUsuario = DataAccessTranslatorPin.translateEntityPinUsuario(pin)

              DataAccessAdapterUsuario.crearUsuarioPin(pinUsuario)

              new SmtpServiceClient().send(buildMessage(pin, UsuarioMessage(correoCliente, message.identificacion, message.tipoIdentificacion,null, false, None), "alianza.smtp.templatepin.reiniciarContrasena", "alianza.smtp.asunto.reiniciarContrasena"), (_, _) => Unit)

            }
          case zFailure(error) =>
            error match {
              case errorPersistence: ErrorPersistence => currentSender ! errorPersistence.exception
              case errorVal: ErrorValidacion =>
                currentSender ! ResponseMessage(Conflict, errorVal.msg)
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
      currentSender ! JsonUtil.toJson(response)
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


  private def validaSolicitud(message:UsuarioMessage): Future[Validation[ErrorValidacion, Cliente]] = {

    val consultaNumDocFuture = validacionConsultaNumDoc(message)
    val consultaCorreoFuture: Future[Validation[ErrorValidacion, Unit.type]] = validacionConsultaCorreo(message)
    val consultaClienteFuture: Future[Validation[ErrorValidacion, Cliente]] = validacionConsultaCliente(message)
    val validacionClave: Future[Validation[ErrorValidacion, Unit.type]] = validacionReglasClave(message)

    (for{
      resultValidacionClave <- ValidationT(validacionClave)
      resultConsultaNumDoc <- ValidationT(consultaNumDocFuture)
      resultConsultaCorreo <- ValidationT(consultaCorreoFuture)
      cliente <- ValidationT(consultaClienteFuture)
    }yield {
      cliente
    }).run
  }

  private def guardarUsuario(message:UsuarioMessage): Future[Validation[ErrorValidacion, Int]] = {
    val passwordUserWithAppend = message.contrasena.concat( AppendPasswordUser.appendUsuariosFiducia );
    println("Password User Final***->"+passwordUserWithAppend)
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


  private def buildMessage(pinUsuario: PinUsuario, message: UsuarioMessage, templateBody: String, asuntoTemp: String) = {
    val body: String =new MailMessageUsuario(templateBody).getMessagePin(pinUsuario)
    val asunto: String = config.getString(asuntoTemp)
    MailMessage(config.getString("alianza.smtp.from"), "josegarcia@seven4n.com", List(), asunto, body, "")
    //MailMessage(config.getString("alianza.smtp.from"), message.correo, List(), asunto, body, "")
  }


  private val errorEstadoReinicioContrasena = ErrorMessage("409.8", "El usuario se encuentra en proceso de reinicio de contrasena", "El usuario se encuentra en proceso de reinicio de contrasena").toJson
  private val errorEstadoUsuarioNoPermitido = ErrorMessage("409.9", "El estado del usuario no permite reiniciar la contrasena", "El estado del usuario no permite reiniciar la contrasena").toJson

}