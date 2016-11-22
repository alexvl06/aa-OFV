package portal.transaccional.autenticacion.service.drivers.autorizacion

import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.commons.enumerations.TiposCliente.TiposCliente
import co.com.alianza.exceptions._
import co.com.alianza.infrastructure.dto.{ Usuario, UsuarioComercialDTO }
import co.com.alianza.persistence.entities.Usuario
import co.com.alianza.persistence.entities.{ RecursoPerfil, Usuario, UsuarioComercial }
import co.com.alianza.util.json.JsonUtil
import co.com.alianza.util.token.Token
import portal.transaccional.autenticacion.service.drivers.recurso.RecursoRepository
import portal.transaccional.autenticacion.service.drivers.sesion.SesionRepository
import portal.transaccional.autenticacion.service.drivers.usuarioComercial.UsuarioComercialRepository
import portal.transaccional.autenticacion.service.drivers.usuarioIndividual.DataAccessTranslator

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by s4n 2016
 */
case class AutorizacionUsuarioComercialDriverRepository(sesionRepo: SesionRepository, recursoRepo: RecursoRepository, usuarioRepo: UsuarioComercialRepository)(implicit val ex: ExecutionContext) extends AutorizacionUsuarioComercialRepository {

  def invalidarToken(token: String, encriptedToken: String): Future[Int] = {
    for {
      x <- usuarioRepo.eliminarToken(encriptedToken)
      _ <- sesionRepo.eliminarSesion(token)
    } yield x
  }

  def invalidarTokenSAC(token: String, encriptedToken: String): Future[Int] = {
    usuarioRepo.eliminarToken(encriptedToken)
  }

  def autorizarFiduciaria(token: String, encriptedToken: String, url: String): Future[ValidacionAutorizacion] = {
    for {
      validar <- validarToken(token)
      validarSesion <- sesionRepo.validarSesion(token)
      usuarioOption <- usuarioRepo.getByToken(encriptedToken)
      usuario <- validarUsario(usuarioOption, TiposCliente.comercialFiduciaria)
      recursos <- recursoRepo.obtenerRecursosClienteIndividual()
      validarRecurso <- resolveMessageRecursos(usuarioOption, recursos, url, TiposCliente.comercialFiduciaria)
    } yield validarRecurso
  }

  def autorizarValores(token: String, encriptedToken: String, url: String): Future[ValidacionAutorizacion] = {
    for {
      validar <- validarToken(token)
      validarSesion <- sesionRepo.validarSesion(token)
      usuarioOption <- usuarioRepo.getByToken(encriptedToken)
      usuario <- validarUsario(usuarioOption, TiposCliente.comercialValores)
      recursos <- recursoRepo.obtenerRecursosClienteIndividual()
      validarRecurso <- resolveMessageRecursos(usuarioOption, recursos, url, TiposCliente.comercialValores)
    } yield validarRecurso
  }

  def autorizarSAC(token: String, encriptedToken: String, url: String): Future[ValidacionAutorizacion] = {
    for {
      validar <- validarToken(token, false)
      usuarioOption <- usuarioRepo.getByToken(encriptedToken)
      usuario <- validarUsario(usuarioOption, TiposCliente.comercialSAC)
      recursos <- recursoRepo.obtenerRecursosClienteIndividual()
      validarRecurso <- resolveMessageRecursos(usuarioOption, recursos, url, TiposCliente.comercialSAC)
    } yield validarRecurso
  }

  private def validarUsario(usuarioOption: Option[UsuarioComercial], tipoCliente: TiposCliente): Future[ValidacionAutorizacion] = {
    usuarioOption match {
      case Some(usuario: UsuarioComercial) =>
        val usuarioDTO: UsuarioComercialDTO = UsuarioComercialDTO(tipoCliente, usuario.id, usuario.usuario)
        val usuarioJson: String = JsonUtil.toJson(usuarioDTO)
        Future.successful(AutorizadoComercial(usuarioJson))
      case _ => Future.failed(NoAutorizado("usuario no encontrado"))
    }
  }

  private def validarToken(token: String, validarExpiracionToken: Boolean = true): Future[Boolean] = {
    Token.autorizarToken(token, validarExpiracionToken) match {
      case true => Future.successful(true)
      case _ => Future.failed(NoAutorizado("Token erróneo"))
    }
  }

  /**
   * De acuerdo si la lista tiene contenido retorna un ResponseMessage
   *
   * @param recursos Listado de recursos
   * @return
   */
  private def resolveMessageRecursos(usuarioOption: Option[UsuarioComercial], recursos: Seq[RecursoPerfil], url: String, tipoCliente: TiposCliente): Future[ValidacionAutorizacion] = Future {
    val recursosFiltro: Seq[RecursoPerfil] = {
      if (url.nonEmpty && !url.startsWith("/comercial")) recursoRepo.filtrarRecursos(recursos, url)
      else Seq(RecursoPerfil(0, url, false, None))
    }
    val usuarioDTO: UsuarioComercialDTO = UsuarioComercialDTO(tipoCliente, usuarioOption.get.id, usuarioOption.get.usuario)
    recursosFiltro.nonEmpty match {
      case false =>
        val usuarioForbidden: ForbiddenMessage = ForbiddenMessage(usuarioDTO, None)
        Prohibido("403.1", JsonUtil.toJson(usuarioForbidden))
      case true =>
        recursosFiltro.head.filtro match {
          case filtro @ Some(_) =>
            val usuarioForbidden: ForbiddenMessage = ForbiddenMessage(usuarioDTO, filtro)
            Prohibido("403.2", JsonUtil.toJson(usuarioForbidden))
          case None =>
            val usuarioJson: String = JsonUtil.toJson(usuarioDTO)
            Autorizado(usuarioJson)
        }
    }
  }

  case class ForbiddenMessage(usuario: UsuarioComercialDTO, filtro: Option[String])

}
