package portal.transaccional.autenticacion.service.drivers.autorizacion

import akka.util.Timeout
import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.exceptions._
import co.com.alianza.infrastructure.dto.{ UsuarioEmpresarialAdmin, UsuarioInmobiliarioAuth }
import co.com.alianza.infrastructure.messages.ResponseMessage
import co.com.alianza.persistence.entities.RecursoPerfilClienteAdmin
import co.com.alianza.util.json.JsonUtil
import co.com.alianza.util.token.Token
import enumerations.empresa.EstadosDeEmpresaEnum
import portal.transaccional.autenticacion.service.drivers.recurso.RecursoRepository
import portal.transaccional.autenticacion.service.drivers.sesion.SesionRepository
import portal.transaccional.autenticacion.service.drivers.usuarioAdmin.{ DataAccessTranslator, UsuarioAdminRepository }
import portal.transaccional.autenticacion.service.drivers.usuarioAgenteInmobiliario.AutorizacionRepository
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.AlianzaDAO
import spray.http.StatusCodes._

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.control.NoStackTrace

/**
 * Created by seven4n on 2016
 */
case class AutorizacionUsuarioEmpresarialAdminDriverRepository(adminRepo: UsuarioAdminRepository, sesionRepo: SesionRepository,
  alianzaDAO: AlianzaDAO, recursoRepo: RecursoRepository, authInmobiliario: AutorizacionRepository)(implicit val ex: ExecutionContext)
    extends AutorizacionUsuarioEmpresarialAdminRepository {

  implicit val timeout = Timeout(5.seconds)

  def autorizar(token: String, encriptedToken: String, url: String, ip: String, tipoCliente: String): Future[NoStackTrace] = {
    for {
      _ <- validarToken(token)
      _ <- sesionRepo.validarSesion(token)
      sesion <- sesionRepo.obtenerSesion(token)
      adminEstado <- alianzaDAO.getByTokenAdmin(encriptedToken)
      _ <- validarEstadoEmpresa(adminEstado._2)
      result <- obtenerRecursos(DataAccessTranslator.entityToDto(adminEstado._1, tipoCliente), tipoCliente, url)
    } yield result
  }

  def invalidarToken(token: String, encriptedToken: String): Future[Int] = {
    for {
      x <- adminRepo.invalidarToken(encriptedToken)
      _ <- sesionRepo.eliminarSesion(token)
    } yield x
  }

  private def validarToken(token: String): Future[Boolean] = {
    Token.autorizarToken(token) match {
      case true => Future.successful(true)
      case false => Future.failed(ValidacionException("401.24", "Error token"))
    }
  }

  private def validarEstadoEmpresa(estado: Int): Future[ResponseMessage] = {
    val empresaActiva: Int = EstadosDeEmpresaEnum.activa.id
    estado match {
      case `empresaActiva` => Future.successful(ResponseMessage(OK, "Empresa Activa"))
      case _ => Future.failed(ValidacionException("401.23", "Error sesión"))
    }
  }

  private def obtenerRecursos(adminDTO: UsuarioEmpresarialAdmin, tipoCliente: String, url: String): Future[NoStackTrace] = {
    if (tipoCliente == TiposCliente.clienteAdministrador.toString) {
      for {
        recursos <- alianzaDAO.getAdminResources(adminDTO.id)
        result <- resolveMessageRecursos(adminDTO, recursos, url)
      } yield result
    } else {
      for {
        recursos <- alianzaDAO.get4()
        result <- {
          val authUser = UsuarioInmobiliarioAuth(adminDTO.id, adminDTO.tipoCliente, adminDTO.identificacion, adminDTO.tipoIdentificacion, adminDTO.usuario)
          authInmobiliario.filtrarRecuros(authUser, recursos, Option(url))
        }
      } yield result
    }
  }

  /**
   * De acuerdo si la lista tiene contenido retorna un ResponseMessage
   *
   * @param recursos Listado de recursos
   * @return
   */
  private def resolveMessageRecursos(adminDTO: UsuarioEmpresarialAdmin, recursos: Seq[RecursoPerfilClienteAdmin], url: String): Future[ValidacionAutorizacion] = Future {
    //si la url es "", viene desde el mismo componente, por lo tanto no hay que hacer filtro alguno
    val recursosFiltro: Seq[RecursoPerfilClienteAdmin] = {
      if (url.nonEmpty) recursoRepo.filtrarRecursosClienteAdmin(recursos, url)
      else Seq(RecursoPerfilClienteAdmin(0, url, false, None))
    }
    recursosFiltro.nonEmpty match {
      case false =>
        val usuarioForbidden: ForbiddenMessageAdmin = ForbiddenMessageAdmin(adminDTO, None)
        Prohibido("403.1", JsonUtil.toJson(usuarioForbidden))
      case true =>
        recursosFiltro.head.filtro match {
          case filtro @ Some(_) =>
            val usuarioForbidden: ForbiddenMessageAdmin = ForbiddenMessageAdmin(adminDTO, filtro)
            Prohibido("403.2", JsonUtil.toJson(usuarioForbidden))
          case None =>
            val usuarioJson: String = JsonUtil.toJson(adminDTO)
            Autorizado(usuarioJson)
        }
    }
  }
}

case class ForbiddenMessageAdmin(usuario: UsuarioEmpresarialAdmin, filtro: Option[String])
