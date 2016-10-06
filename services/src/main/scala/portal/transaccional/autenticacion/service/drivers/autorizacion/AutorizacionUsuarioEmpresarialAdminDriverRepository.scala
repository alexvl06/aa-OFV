package portal.transaccional.autenticacion.service.drivers.autorizacion

import akka.util.Timeout
import co.com.alianza.exceptions.{ Autorizado, Prohibido, ValidacionAutorizacion, ValidacionException }
import co.com.alianza.infrastructure.dto.UsuarioEmpresarialAdmin
import co.com.alianza.infrastructure.messages.ResponseMessage
import co.com.alianza.persistence.entities.RecursoPerfilClienteAdmin
import co.com.alianza.util.json.JsonUtil
import co.com.alianza.util.token.Token
import enumerations.empresa.EstadosDeEmpresaEnum
import portal.transaccional.autenticacion.service.drivers.recurso.RecursoRepository
import portal.transaccional.autenticacion.service.drivers.sesion.SesionRepository
import portal.transaccional.autenticacion.service.drivers.usuarioAdmin.{ DataAccessTranslator, UsuarioEmpresarialAdminRepository }
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.AlianzaDAO
import spray.http.StatusCodes._

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by seven4n on 2016
 */
case class AutorizacionUsuarioEmpresarialAdminDriverRepository(adminRepo: UsuarioEmpresarialAdminRepository, sesionRepo: SesionRepository,
    alianzaDAO: AlianzaDAO, recursoRepo: RecursoRepository)(implicit val ex: ExecutionContext) extends AutorizacionUsuarioEmpresarialAdminRepository {

  implicit val timeout = Timeout(5.seconds)

  def autorizar(token: String, encriptedToken: String, url: String, ip: String): Future[ValidacionAutorizacion] = {
    for {
      _ <- validarToken(token)
      _ <- sesionRepo.validarSesion(token)
      sesion <- sesionRepo.obtenerSesion(token)
      adminEstado <- alianzaDAO.getByTokenAdmin(encriptedToken)
      _ <- validarEstadoEmpresa(adminEstado._2)
      recursos <- alianzaDAO.getAdminResources(adminEstado._1.id)
      result <- resolveMessageRecursos(DataAccessTranslator.entityToDto(adminEstado._1), recursos, url)
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

  /**
   * De acuerdo si la lista tiene contenido retorna un ResponseMessage
   *
   * @param recursos Listado de recursos
   * @return
   */
  private def resolveMessageRecursos(adminDTO: UsuarioEmpresarialAdmin, recursos: Seq[RecursoPerfilClienteAdmin], url: String): Future[ValidacionAutorizacion] = Future {

    println("ruta => ", url)

    val recursosFiltro = recursoRepo.filtrarRecursosClienteAdmin(recursos, url)
    recursosFiltro.nonEmpty match {
      case false =>
        println("false")
        val usuarioForbidden: ForbiddenMessageAdmin = ForbiddenMessageAdmin(adminDTO, None)
        Prohibido("403.1", JsonUtil.toJson(usuarioForbidden))
      case true =>
        println("true")
        recursos.head.filtro match {
          case filtro @ Some(_) =>
            println("prohibido")
            val usuarioForbidden: ForbiddenMessageAdmin = ForbiddenMessageAdmin(adminDTO, filtro)
            Prohibido("403.2", JsonUtil.toJson(usuarioForbidden))
          case None =>
            println("ok")
            val usuarioJson: String = JsonUtil.toJson(adminDTO)
            Autorizado(usuarioJson)
        }
    }
  }
}

case class ForbiddenMessageAdmin(usuario: UsuarioEmpresarialAdmin, filtro: Option[String])
