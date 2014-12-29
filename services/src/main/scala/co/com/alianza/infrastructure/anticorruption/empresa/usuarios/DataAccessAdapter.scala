package co.com.alianza.infrastructure.anticorruption.empresa.usuarios

import java.sql.Timestamp



import scalaz.Validation
import scala.concurrent.{ExecutionContext, Future}
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.app.MainActors
import scalaz.{Failure => zFailure, Success => zSuccess}
import co.com.alianza.infrastructure.dto._
import co.com.alianza.persistence.entities.{UsuarioEmpresarial => eUsuario}
import com.typesafe.config.Config
import co.com.alianza.persistence.messages.empresa.GetUsuariosEmpresaBusquedaRequest
import co.com.alianza.persistence.repositories.empresa.UsuariosEmpresaRepository
import co.com.alianza.persistence.messages.empresa.GetUsuariosEmpresaBusquedaRequest
import co.com.alianza.infrastructure.dto.Usuario

object DataAccessAdapter {

  implicit val ec: ExecutionContext = MainActors.dataAccesEx
  private implicit val conf: Config = MainActors.conf




  def obtenerUsuariosBusqueda(message:GetUsuariosEmpresaBusquedaRequest): Future[Validation[PersistenceException, List[UsuarioEmpresarial]]] = {
    val repo = new UsuariosEmpresaRepository()
    repo.obtenerUsuariosBusqueda(message.correo, message.identificacion, message.estado, message.idClienteAdmin) map {
      x => transformValidationList(x)
    }
  }


  private def transformValidationList(origin: Validation[PersistenceException, List[eUsuario]]): Validation[PersistenceException, List[UsuarioEmpresarial]] = {
    origin match {
      case zSuccess(response: List[eUsuario]) => zSuccess(DataAccessTranslator.translateUsuario(response))
      case zFailure(error)    =>  zFailure(error)
    }
  }

}


