package co.com.alianza.infrastructure.anticorruption.empresa.usuarios

import java.sql.Timestamp

import co.com.alianza.constants.{AppendPasswordUser, EstadosUsuarioEnum}
import co.com.alianza.infrastructure.messages.{GetUsuariosBusquedaMessage, AgregarIpUsuarioMessage}

import scalaz.Validation
import scala.concurrent.{ExecutionContext, Future}
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.app.MainActors
import scalaz.{Failure => zFailure, Success => zSuccess}
import co.com.alianza.infrastructure.dto.{UsuarioAdmin, IpUsuario, Usuario, UsuarioEmpresarialAdmin}
import co.com.alianza.persistence.entities.{Usuario => eUsuario, UsuarioAdmin => eUsuarioAdmin, UsuarioEmpresarialAdmin => eUsuarioEmpresarialAdmin, IpUsuario => eIpUsuario, PinUsuario => ePinUsuario, PerfilUsuario}
import co.com.alianza.persistence.repositories.{UsuariosRepository, UsuarioEmpresarialAdminRepository}
import scala.util.Try
import co.com.alianza.util.clave.Crypto
import com.typesafe.config.Config
import co.com.alianza.persistence.messages.empresa.GetUsuariosEmpresaBusquedaRequest

object DataAccessAdapter {

  implicit val ec: ExecutionContext = MainActors.dataAccesEx
  private implicit val conf: Config = MainActors.conf




  def obtenerUsuariosBusqueda(message:GetUsuariosEmpresaBusquedaRequest): Future[Validation[PersistenceException, List[Usuario]]] = {
    val repo = new UsuariosRepository()
    repo.obtenerUsuariosBusqueda(message.correo, message.tipoIdentificacion, message.identificacion, message.estadoUsuario) map {
      x => transformValidationList(x)
    }
  }


  private def transformValidationList(origin: Validation[PersistenceException, List[eUsuario]]): Validation[PersistenceException, List[Usuario]] = {
    origin match {
      case zSuccess(response: List[eUsuario]) => zSuccess(DataAccessTranslator.translateUsuario(response))
      case zFailure(error)    =>  zFailure(error)
    }
  }

}


