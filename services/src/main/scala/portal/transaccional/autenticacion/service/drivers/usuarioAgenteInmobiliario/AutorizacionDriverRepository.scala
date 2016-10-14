package portal.transaccional.autenticacion.service.drivers.usuarioAgenteInmobiliario

import co.com.alianza.exceptions.{ Autorizado, Prohibido, ValidacionAutorizacion, ValidacionException }
import co.com.alianza.infrastructure.dto.UsuarioEmpresarialAdmin
import co.com.alianza.infrastructure.messages.ResponseMessage
import co.com.alianza.persistence.entities.RecursoPerfilClienteAdmin
import co.com.alianza.util.json.JsonUtil
import co.com.alianza.util.token.Token
import enumerations.empresa.EstadosDeEmpresaEnum
import portal.transaccional.autenticacion.service.drivers.autorizacion.{ AutorizacionUsuarioEmpresarialAdminRepository, ForbiddenMessageAdmin }
import portal.transaccional.autenticacion.service.drivers.recurso.RecursoRepository
import portal.transaccional.autenticacion.service.drivers.sesion.SesionRepository
import portal.transaccional.autenticacion.service.drivers.usuarioAdmin.{ DataAccessTranslator, UsuarioEmpresarialAdminRepository }
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.AlianzaDAO
import spray.http.StatusCodes._

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by alexandra on 13/10/16.
 */
case class AutorizacionDriverRepository(adminRepo: UsuarioEmpresarialAdminRepository, sesionRepo: SesionRepository,
    alianzaDAO: AlianzaDAO, recursoRepo: RecursoRepository)(implicit val ex: ExecutionContext) extends AutorizacionUsuarioEmpresarialAdminRepository {

  def autorizar(token: String, encriptedToken: String, url: Option[String], ip: String): Future[ValidacionAutorizacion] = {
    for {
      _ <- validarToken(token)
      _ <- sesionRepo.validarSesion(token)
      sesion <- sesionRepo.obtenerSesion(token)
      adminEstado <- alianzaDAO.getByTokenAdmin(encriptedToken)
      _ <- validarEstadoEmpresa(adminEstado._2)
      recursos <- alianzaDAO.getAdminResources(adminEstado._1.id)
      result <- resolveMessageRecursos(DataAccessTranslator.entityToDto(adminEstado._1), recursos, url.getOrElse(""))
    } yield result
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

  /**
   * De acuerdo si la lista tiene contenido retorna un ResponseMessage
   *
   * @param recursos Listado de recursos
   * @return
   */
  private def resolveMessageRecursos(adminDTO: UsuarioEmpresarialAdmin, recursos: Seq[RecursoPerfilClienteAdmin], url: String): Future[ValidacionAutorizacion] = Future {
    val recursosFiltro = recursoRepo.filtrarRecursosClienteAdmin(recursos, url)

    recursosFiltro.nonEmpty match {
      case false =>
        val usuarioForbidden: ForbiddenMessageAdmin = ForbiddenMessageAdmin(adminDTO, None)
        Prohibido("403.1", JsonUtil.toJson(usuarioForbidden))
      case true =>
        recursos.head.filtro match {
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
