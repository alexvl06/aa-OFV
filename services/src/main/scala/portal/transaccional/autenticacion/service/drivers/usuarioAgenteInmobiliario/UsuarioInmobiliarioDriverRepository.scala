package portal.transaccional.autenticacion.service.drivers.usuarioAgenteInmobiliario

import java.sql.Timestamp

import akka.actor.ActorSystem
import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.constants.TiposConfiguracion
import co.com.alianza.exceptions.ValidacionExceptionPasswordRules
import co.com.alianza.microservices.MailMessage
import co.com.alianza.persistence.entities.{ PinAgenteInmobiliario, UsuarioAgenteInmobiliario, UsuarioAgenteInmobiliarioTable }
import com.typesafe.config.Config
import enumerations.EstadosUsuarioEnumInmobiliario._
import enumerations.{ EstadosUsuarioEnum, EstadosUsuarioEnumInmobiliario, TipoAgenteInmobiliario }
import portal.transaccional.autenticacion.service.drivers.usuarioAgente.{ UsuarioEmpresarialRepository, UsuarioEmpresarialRepositoryG }
import portal.transaccional.autenticacion.service.web.agenteInmobiliario.{ ConsultarAgenteInmobiliarioListResponse, ConsultarAgenteInmobiliarioResponse, PaginacionMetadata }
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal._

import scala.concurrent.{ ExecutionContext, Future }

case class UsuarioInmobiliarioDriverRepository(
  configDao: ConfiguracionDAOs,
  constructoresDao: UsuarioEmpresarialAdminDAOs,
  usuariosDao: UsuarioAgenteInmobDAO,
  pinRepository: UsuarioInmobiliarioPinRepository
)(implicit val ex: ExecutionContext, system: ActorSystem, config: Config)
  extends UsuarioEmpresarialRepositoryG[UsuarioAgenteInmobiliarioTable, UsuarioAgenteInmobiliario](usuariosDao)
    with UsuarioEmpresarialRepository[UsuarioAgenteInmobiliario] with UsuarioInmobiliarioRepository {

  override def createAgenteInmobiliario(idConstructor: Int, tipoIdentificacion: Int, identificacion: String, correo: String, usuario: String,
    nombre: Option[String], cargo: Option[String], descripcion: Option[String], tipoAgente: String): Future[Int] = {
    // se verifica que el agente inmobiliario no se vaya a crear con el mismo nombre de usuario del constructor
    // y que no haya sido creado previamente
    val proceder: Future[Boolean] = validateUniqueness(idConstructor, identificacion, usuario, tipoAgente)

    proceder.flatMap({
      case false => Future.successful(0)
      case true =>
        val agente = UsuarioAgenteInmobiliario(
          0, identificacion, tipoIdentificacion, usuario, correo, EstadosUsuarioEnum.pendienteActivacion.id,
          None, None, new Timestamp(System.currentTimeMillis()), 0, None, nombre, cargo, descripcion, None, tipoAgente)
        for {
          idAgente <- usuariosDao.create(agente)
          configExpiracion <- configDao.getByKey(TiposConfiguracion.EXPIRACION_PIN.llave)
          pinAgente: PinAgenteInmobiliario = pinRepository.generarPinAgente(configExpiracion, idAgente)
          idPin <- pinRepository.asociarPinAgente(pinAgente)
          correoActivacion: MailMessage = pinRepository.generarCorreoActivacion(
            pinAgente.tokenHash,
            configExpiracion.valor.toInt, nombre.getOrElse(usuario), usuario, correo
          )
        } yield {
          // el envío del correo se ejecuta de forma asíncrona dado que no interesa el éxito de la operación,
          // es decir, si el envío falla, no se debería responder con error la creación del agente inmobiliario
          pinRepository.enviarEmail(correoActivacion)
          // se retorna el id del agente generado
          idAgente
        }
    })
  }

  private def validateUniqueness(idConstructor : Int, identificacion: String, usuario: String, tipoAgente: String): Future[Boolean] = {

    if(tipoAgente != TipoAgenteInmobiliario.empresarial.toString) {
      usuariosDao.exists(0, identificacion, usuario).map(!_)
    } else {
      for {
        constructorOp <- constructoresDao.getById(idConstructor)
        existeAgente <- usuariosDao.exists(0, identificacion, usuario)
      } yield constructorOp.isDefined && constructorOp.get.usuario != usuario && !existeAgente
    }
  }

  override def getAgenteInmobiliario(id: Int): Future[Option[UsuarioAgenteInmobiliario]] = {
    usuariosDao.get(id)
  }

  override def getAgenteInmobiliario(
    identificacion: String,
    usuario: String
  ): Future[Option[ConsultarAgenteInmobiliarioResponse]] = {
    usuariosDao.get(identificacion, usuario).map(_.map(agente => ConsultarAgenteInmobiliarioResponse(
      agente.id,
      agente.correo,
      agente.usuario,
      agente.estado,
      agente.nombre,
      agente.cargo,
      agente.descripcion,
      agente.tipoAgente
    )))
  }

  override def getAgenteInmobiliarioList(identificacion: String, nombre: Option[String], usuario: Option[String],
    correo: Option[String], estado: Option[String],
    pagina: Option[Int], itemsPorPagina: Option[Int], ordenarPor: Option[String]): Future[ConsultarAgenteInmobiliarioListResponse] = {
    usuariosDao
      .getAll(identificacion, nombre, usuario, correo, estado, pagina, itemsPorPagina, ordenarPor)
      .map(res => {
        val agentes: Seq[ConsultarAgenteInmobiliarioResponse] = res._5.map(agente => ConsultarAgenteInmobiliarioResponse(
          agente.id, agente.correo, agente.usuario, agente.estado,
          agente.nombre, agente.cargo, agente.descripcion, agente.tipoAgente
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
    usuariosDao.get(identificacion, usuario).flatMap {
      case None => Future.successful(0)
      case Some(agente) =>
        val correoCambio: Boolean = correo != agente.correo
        usuariosDao.update(identificacion, usuario, correo, nombre, cargo, descripcion).flatMap { r =>
          if (!correoCambio) {
            Future.successful(r)
          } else {
            for {
              configExpiracion <- configDao.getByKey(TiposConfiguracion.EXPIRACION_PIN.llave)
              pinAgente: PinAgenteInmobiliario = pinRepository.generarPinAgente(configExpiracion, agente.id, reinicio = true)
              idPin <- pinRepository.asociarPinAgente(pinAgente)
              correoReinicio: MailMessage = pinRepository.generarCorreoReinicio(
                pinAgente.tokenHash,
                configExpiracion.valor.toInt, nombre.getOrElse(usuario), correo
              )
            } yield {
              pinRepository.enviarEmail(correoReinicio)
              r
            }
          }
        }
    }
  }

  override def activateOrDeactivateAgenteInmobiliario(identificacion: String, usuario: String): Future[Option[ConsultarAgenteInmobiliarioResponse]] = {
    usuariosDao.get(identificacion, usuario).flatMap {
      case None => Future.successful(Option.empty)
      case Some(agente) =>
        (if (agente.estado == EstadosUsuarioEnumInmobiliario.activo.id) {
          Option(EstadosUsuarioEnumInmobiliario.inactivo)
        } else if (agente.estado == EstadosUsuarioEnumInmobiliario.inactivo.id) {
          Option(EstadosUsuarioEnumInmobiliario.activo)
        } else {
          Option.empty
        }).map { estado =>
          usuariosDao.updateState(identificacion, usuario, estado).map {
            case x if x == 0 => Option.empty
            case _ => Option(ConsultarAgenteInmobiliarioResponse(agente.id, agente.correo, agente.usuario, estado.id,
              agente.nombre, agente.cargo, agente.descripcion, agente.tipoAgente))
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

  override def updateContrasena(contrasena: String, idUsuario: Int): Future[Int] = {
    usuariosDao.updateContrasena(contrasena, idUsuario)
  }

  override def updateEstadoAgente(identificacion: String, usuario: String, estado: estadoUsuarioInmobiliario): Future[Int] = {
    usuariosDao.updateState(identificacion, usuario, estado)
  }
}
