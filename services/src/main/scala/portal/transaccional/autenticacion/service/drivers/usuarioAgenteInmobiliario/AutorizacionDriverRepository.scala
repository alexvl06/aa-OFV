package portal.transaccional.autenticacion.service.drivers.usuarioAgenteInmobiliario

import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.exceptions.ValidacionException
import co.com.alianza.infrastructure.dto.UsuarioInmobiliarioAuth
import co.com.alianza.persistence.entities.RecursoBackendInmobiliario
import co.com.alianza.util.token.Token
import portal.transaccional.autenticacion.service.drivers.recurso.RecursoRepository
import portal.transaccional.autenticacion.service.drivers.sesion.SesionRepository
import portal.transaccional.autenticacion.service.drivers.usuarioAdmin.{ DataAccessTranslator => ConstructorDataAccessTranslator }
import portal.transaccional.autenticacion.service.util.ws.{ GenericAutorizado, GenericNoAutorizado, GenericValidacionAutorizacion }
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.AlianzaDAOs

import scala.concurrent.{ ExecutionContext, Future }

case class AutorizacionDriverRepository(sesionRepo: SesionRepository, alianzaDAO: AlianzaDAOs, recursoRepo: RecursoRepository)(implicit val ex: ExecutionContext) extends AutorizacionRepository {

  def autorizar(token: String, encriptedToken: String, url: Option[String], ip: String, tipoCliente: String): Future[GenericValidacionAutorizacion] = {
    val isInterno = if (tipoCliente == TiposCliente.agenteInmobiliarioInterno.toString) true else false
    for {
      _ <- validarToken(token)
      _ <- sesionRepo.validarSesion(token)
      _ <- sesionRepo.obtenerSesion(token)
      agente <- alianzaDAO.getByTokenAgenteInmobiliario(encriptedToken)
      permisoProyecto <- if (isInterno) alianzaDAO.getMenuAdmin(isInterno) else getPermiso(agente.id, url.getOrElse(""), isInterno)
      validacion <- filtrarRecuros(DataAccessTranslator.entityToDto(agente), permisoProyecto, url)
    } yield validacion
  }

  private def validarToken(token: String): Future[Boolean] = {
    Token.autorizarToken(token) match {
      case true => Future.successful(true)
      case false => Future.failed(ValidacionException("401.24", "Error token"))
    }
  }

  // Obtiene los proyectos a los cuales tiene asignados permisos un agenteInmobiliario, en caso de que no se cumpla el regex de fideicomiso,
  // y proyecto en el Path significa que no necesita autorización por proyecto.
  private def getPermiso(idUsuario: Int, url: String, isInterno: Boolean): Future[Seq[RecursoBackendInmobiliario]] = {
    val sacarProyecto = "(/[\\w]*)*/fideicomisos/([0-9]+)/proyectos/([0-9]+)(/[\\w|\\W]*)".r

    url match {
      case sacarProyecto(inicio, fideicomiso, proyecto, fin) =>

        // Probar que pasa si se le mete algo que no sea numero!
        alianzaDAO.getResourcesByProjectAndAgent(idUsuario, proyecto.toInt, fideicomiso.toInt).flatMap { (permisos: Seq[RecursoBackendInmobiliario]) =>
          if (permisos.isEmpty) {
            Future.failed(GenericNoAutorizado("403.1", "El usuario no tiene permisos suficientes para ver información del proyecto."))
          } else {
            Future.successful(permisos)
          }
        }
      case _ => alianzaDAO.getBackResourcesByAgent(idUsuario)
    }

  }

  def filtrarRecuros(
    agente: UsuarioInmobiliarioAuth,
    recursos: Seq[RecursoBackendInmobiliario],
    request: Option[String]
  ): Future[GenericValidacionAutorizacion] = {

    val usuarioExitoso = Future.successful(GenericAutorizado[UsuarioInmobiliarioAuth](agente))
    val usuarioNoExitoso = Future.failed(GenericNoAutorizado("403.1", s"El usuario no tiene permisos suficientes para ingresar al servicio." + request.getOrElse("")))

    request match {
      case Some(url) =>
        val isMatchUrl = recursos.exists(recurso => recursoRepo.filtrarRecurso(recurso.url, url))
        if (isMatchUrl) usuarioExitoso else usuarioNoExitoso

      case None => usuarioExitoso
    }
  }

}