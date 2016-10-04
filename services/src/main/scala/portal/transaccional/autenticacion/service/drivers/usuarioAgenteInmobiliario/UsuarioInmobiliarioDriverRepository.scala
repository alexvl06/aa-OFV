package portal.transaccional.autenticacion.service.drivers.usuarioAgenteInmobiliario

import java.sql.Timestamp

import akka.actor.ActorSystem
import co.com.alianza.constants.TiposConfiguracion
import co.com.alianza.domain.aggregates.empresa.MailMessageEmpresa
import co.com.alianza.exceptions.ValidacionExceptionPasswordRules
import co.com.alianza.microservices.{MailMessage, SmtpServiceClient}
import co.com.alianza.persistence.entities.{Configuraciones, PinAgenteInmobiliario, UsuarioAgenteInmobiliario, UsuarioAgenteInmobiliarioTable}
import co.com.alianza.util.token.{PinData, TokenPin}
import com.typesafe.config.Config
import enumerations.{EstadosUsuarioEnum, UsoPinEmpresaEnum}
import org.joda.time.DateTime
import portal.transaccional.autenticacion.service.drivers.usuarioAgente.{UsuarioEmpresarialRepository, UsuarioEmpresarialRepositoryG}
import portal.transaccional.autenticacion.service.web.agenteInmobiliario.{ConsultarAgenteInmobiliarioListResponse, ConsultarAgenteInmobiliarioResponse, PaginacionMetadata}
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal._

import scala.concurrent.{ExecutionContext, Future}

/**
  * Implementación del repositorio de agentes inmobiliarios
  *
  * @param ex Contexto de ejecución
  */

case class UsuarioAgenteInmobDriverRepository(usuarioDAO: UsuarioAgenteInmobDAO)(implicit val ex: ExecutionContext) extends
  UsuarioEmpresarialRepositoryG[UsuarioAgenteInmobiliarioTable, UsuarioAgenteInmobiliario](usuarioDAO) with
  UsuarioEmpresarialRepository[UsuarioAgenteInmobiliario]

case class UsuarioInmobiliarioDriverRepository(configDao: ConfiguracionDAOs,
                                               pinDao: PinAgenteInmobiliarioDAOs,
                                               usuariosDao: UsuarioAgenteInmobDAOs)
                                              (implicit val ex: ExecutionContext, system: ActorSystem, config: Config) extends UsuarioInmobiliarioRepository {

  override def createAgenteInmobiliario(tipoIdentificacion: Int, identificacion: String,
                                        correo: String, usuario: String,
                                        nombre: Option[String], cargo: Option[String], descripcion: Option[String]): Future[Int] = {
    usuariosDao.exists(0, identificacion, usuario).flatMap({
      case true => Future.successful(0)
      case false =>
        val agente = UsuarioAgenteInmobiliario(
          0, identificacion, tipoIdentificacion, usuario, correo, EstadosUsuarioEnum.pendienteActivacion.id,
          None, None, new Timestamp(System.currentTimeMillis()), 0, None, nombre, cargo, descripcion, None
        )
        for {
          idAgente <- usuariosDao.create(agente)
          configExpiracion <- configDao.getByKey(TiposConfiguracion.EXPIRACION_PIN.llave)
          pinAgente: PinAgenteInmobiliario = generarPinAgente(configExpiracion, idAgente)
          idPin <- pinDao.create(pinAgente)
          correoActivacion: MailMessage = generarCorreoActivacion(pinAgente.tokenHash, configExpiracion.valor.toInt, identificacion, usuario, correo)
        } yield {
          // el envío del correo se ejecuta de forma asíncrona dado que no interesa el éxito de la operación,
          // es decir, si el envío falla, no se debería responder con error la creación del agente inmobiliario
          enviarEmail(correoActivacion)
          // se retorna el id del agente generado
          idAgente
        }
    })
  }

  override def getAgenteInmobiliario(identificacion: String,
                                     usuario: String): Future[Option[ConsultarAgenteInmobiliarioResponse]] = {
    usuariosDao.get(identificacion, usuario).map(_.map(agente => ConsultarAgenteInmobiliarioResponse(
      agente.id,
      agente.correo,
      agente.usuario,
      agente.estado,
      agente.nombre,
      agente.cargo,
      agente.descripcion
    )))
  }

  override def getAgenteInmobiliarioList(identificacion: String, nombre: Option[String], usuario: Option[String],
                                         correo: Option[String], estado: Option[Int],
                                         pagina: Option[Int], itemsPorPagina: Option[Int]): Future[ConsultarAgenteInmobiliarioListResponse] = {
    usuariosDao
      .getAll(identificacion, nombre, usuario, correo, estado, pagina, itemsPorPagina)
      .map(res => {
        val agentes: Seq[ConsultarAgenteInmobiliarioResponse] = res._5.map(agente => ConsultarAgenteInmobiliarioResponse(
          agente.id, agente.correo, agente.usuario, agente.estado,
          agente.nombre, agente.cargo, agente.descripcion
        ))
        ConsultarAgenteInmobiliarioListResponse(
          PaginacionMetadata(res._1, res._2, res._3, res._4),
          agentes
        )
      })
  }

  override def updateAgenteInmobiliario(identificacion: String, usuario: String,
                                        correo: String, nombre: Option[String],
                                        cargo: Option[String], descripcion: Option[String]): Future[Int] = {
    usuariosDao.update(identificacion, usuario, correo, nombre, cargo, descripcion)
  }

  override def activateOrDeactivateAgenteInmobiliario(identificacion: String, usuario: String): Future[Option[ConsultarAgenteInmobiliarioResponse]] = {
    usuariosDao.get(identificacion, usuario).flatMap {
      case None => Future.successful(Option.empty)
      case Some(agente) =>
        (if (agente.estado == EstadosUsuarioEnum.activo.id) {
          Option.apply(EstadosUsuarioEnum.pendienteActivacion)
        } else if (agente.estado == EstadosUsuarioEnum.pendienteActivacion.id) {
          Option.apply(EstadosUsuarioEnum.activo)
        } else {
          Option.empty
        }).map { estado =>
          usuariosDao.updateState(identificacion, usuario, estado).map {
            case x if x == 0 => Option.empty
            case _ => Option.apply(ConsultarAgenteInmobiliarioResponse(
              agente.id, agente.correo, agente.usuario, estado.id,
              agente.nombre, agente.cargo, agente.descripcion
            ))
          }
        }.getOrElse(Future.successful(Option.empty))
    }
  }

  override def getContrasena(contrasena: String, idUsuario: Int): Future[UsuarioAgenteInmobiliario] = {
    usuariosDao.getContrasena(contrasena, idUsuario).flatMap {
      case Some(agente) => Future.successful(agente)
      case None => Future.failed(ValidacionExceptionPasswordRules("409.7", "No existe la contrasena actual", "", "", ""))
    }
  }

  def updateContrasena(contrasena: String, idUsuario: Int): Future[Int] = usuariosDao.updateContrasena(contrasena, idUsuario)

  def generarPinAgente(configExpiracion: Configuraciones, idUsuario: Int): PinAgenteInmobiliario = {
    val fechaExpiracion: DateTime = new DateTime().plusHours(configExpiracion.valor.toInt)
    val (pin: PinData, usoPin: Int) = (TokenPin.obtenerToken(fechaExpiracion.toDate), UsoPinEmpresaEnum.creacionAgenteInmobiliario.id)
    PinAgenteInmobiliario(None, idUsuario, pin.token, fechaExpiracion, pin.tokenHash.getOrElse(""), usoPin)
  }

  def generarCorreoActivacion(pin: String, caducidad: Int, identificacion: String, usuario: String, correo: String): MailMessage = {
    val remitente: String = config.getString("alianza.smtp.from")
    val asunto: String = config.getString("alianza.smtp.asunto.creacionAgenteInmobiliario")
    val cuerpo: String = new MailMessageEmpresa("alianza.smtp.templatepin.creacionAgenteInmobiliario")
      .getMessagePinCreacionAgenteInmobiliario(pin, caducidad, identificacion, usuario)
    MailMessage(remitente, correo, Nil, asunto, cuerpo, "")
  }

  def enviarEmail(correoActivacion: MailMessage): Unit = {
    new SmtpServiceClient().send(correoActivacion, (_, _) => 1)
    ()
  }
}
