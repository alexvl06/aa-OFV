package co.com.alianza.infrastructure.anticorruption.usuarios

import java.sql.Timestamp

import co.com.alianza.persistence.repositories.{IpsUsuarioRepository, UsuariosRepository, UsuariosEmpresarialRepository}
import scalaz.Validation
import scala.concurrent.{ExecutionContext, Future}
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.app.MainActors
import scalaz.{Failure => zFailure, Success => zSuccess}
import co.com.alianza.infrastructure.dto.{Usuario, UsuarioEmpresarial}
import co.com.alianza.persistence.entities.{Usuario => eUsuario, PinUsuario => ePinUsuario , PerfilUsuario, IpsUsuario, UsuarioEmpresarial => eUsuarioEmpresarial}
import co.com.alianza.persistence.messages.AutenticacionRequest
import enumerations.EstadosUsuarioEnum

object DataAccessAdapter {

  implicit val ec: ExecutionContext = MainActors.dataAccesEx

  def obtenerUsuarios(): Future[Validation[PersistenceException, List[Usuario]]] = {
    val repo = new UsuariosRepository()
    repo.obtenerUsuarios() map {
      x => transformValidationList(x)
    }
  }

  def crearUsuario(usuario:eUsuario): Future[Validation[PersistenceException, Int]] = {
    val repo = new UsuariosRepository()
    repo.guardar(usuario)
  }

  def relacionarIp(idUsuario:Int, ip:String): Future[Validation[PersistenceException, String]] = {
    val repo = new IpsUsuarioRepository()
    repo.guardar(IpsUsuario(idUsuario, ip))
  }


  def obtenerUsuarioNumeroIdentificacion( numeroIdentificacion:String): Future[Validation[PersistenceException, Option[Usuario]]] = {
    val repo = new UsuariosRepository()
    repo.obtenerUsuarioNumeroIdentificacion( numeroIdentificacion ) map {
      x => transformValidation(x)
    }
  }

  def obtieneUsuarioEmpresarialPorNitYUsuario (nit: String, usuario: String): Future[Validation[PersistenceException, Option[UsuarioEmpresarial]]] = {
    new UsuariosEmpresarialRepository().obtieneUsuarioEmpresaPorNitYUsuario(nit, usuario) map {
      x => transformValidationUsuarioEmpresarial(x)
    }
  }

  def obtenerUsuarioId( idUsuario:Int ): Future[Validation[PersistenceException, Option[Usuario]]] = {
    val repo = new UsuariosRepository()
    repo.obtenerUsuarioId( idUsuario ) map {
      x => transformValidation(x)
    }
  }

  def obtenerUsuarioToken( token:String ): Future[Validation[PersistenceException, Option[Usuario]]] = {
    val repo = new UsuariosRepository()
    repo.obtenerUsuarioToken( token ) map {
      x => transformValidation(x)
    }
  }

  def obtenerUsuarioCorreo( correo:String): Future[Validation[PersistenceException, Option[Usuario]]] = {
    val repo = new UsuariosRepository()
    repo.obtenerUsuarioCorreo( correo ) map {
      x => transformValidation(x)
    }
  }

  def ConsultaContrasenaActual( pw_actual: String, idUsuario: Int): Future[Validation[PersistenceException, Option[Usuario]]] = {
    val repo = new UsuariosRepository()
    repo.consultaContrasenaActual( pw_actual, idUsuario ) map {
      x => transformValidation(x)
    }
  }

  def asociarTokenUsuario(numeroIdentificacion:String, token:String): Future[Validation[PersistenceException, Int]] = {
    val repo = new UsuariosRepository()
    repo.asociarTokenUsuario( numeroIdentificacion, token )
  }


  def invalidarTokenUsuario(token:String): Future[Validation[PersistenceException, Int]] = {
    val repo = new UsuariosRepository()
    repo.invalidarTokenUsuario( token )
  }

  def actualizarNumeroIngresosErroneos( numeroIdentificacion:String, numeroIntentos:Int  ): Future[Validation[PersistenceException, Int]] = {
    val repo = new UsuariosRepository()
    repo.actualizarNumeroIngresosErroneos( numeroIdentificacion, numeroIntentos )
  }

  def actualizarIpUltimoIngreso( numeroIdentificacion:String, ipActual:String  ): Future[Validation[PersistenceException, Int]] = {
    val repo = new UsuariosRepository()
    repo.actualizarIpUltimoIngreso( numeroIdentificacion, ipActual )
  }

  def actualizarFechaUltimoIngreso( numeroIdentificacion:String, fechaActual: Timestamp  ): Future[Validation[PersistenceException, Int]] = {
    val repo = new UsuariosRepository()
    repo.actualizarFechaUltimoIngreso( numeroIdentificacion, fechaActual )
  }

  def actualizarEstadoConfronta( numeroIdentificacion:String): Future[Validation[PersistenceException, Int]] = {
    val repo = new UsuariosRepository()
    repo.actualizarEstadoUsuario( numeroIdentificacion,EstadosUsuarioEnum.activo.id )
  }

  def obtenerIpsUsuario( idUsuario:Int ) : Future[Validation[PersistenceException, Vector[IpsUsuario]]] = {
    val repo = new IpsUsuarioRepository()
    repo.obtenerIpsUsuario( idUsuario )
  }

  def agregarIpUsuario( ip:IpsUsuario ) : Future[Validation[PersistenceException, String]] = {
    val repo = new IpsUsuarioRepository()
    repo.guardar( ip )
  }

  def eliminarIpUsuario( ip:IpsUsuario ) : Future[Validation[PersistenceException, Int]] = {
    val repo = new IpsUsuarioRepository()
    repo.eliminar(ip)
  }

  def obtenerIpUsuarioValida( idUsuario:Int, ip:String ) : Future[Validation[PersistenceException, Option[IpsUsuario]]] = {
    val repo = new IpsUsuarioRepository()
    repo.obtenerIpUsuario(idUsuario, ip)
  }

  def actualizarEstadoUsuario( numeroIdentificacion:String, estado:Int ) : Future[Validation[PersistenceException, Int]] = {
    val repo = new UsuariosRepository()
    repo.actualizarEstadoUsuario( numeroIdentificacion, estado )
  }

  def actualizarEstadoUsuario( idUsuario:Int, estado:Int ) : Future[Validation[PersistenceException, Int]] = {
    val repo = new UsuariosRepository()
    repo.actualizarEstadoUsuario( idUsuario, estado )
  }

  def cambiarPassword (idUsuario: Int, password: String): Future[Validation[PersistenceException, Int]] = {
    val repo = new UsuariosRepository()
    repo.cambiarPassword(idUsuario, password)
  }

  def asociarPerfiles (perfiles:List[PerfilUsuario]): Future[Validation[PersistenceException, List[Int]]] = {
    val repo = new UsuariosRepository()
    repo.asociarPerfiles(perfiles)
  }


  def crearUsuarioPin(pinUsuario:ePinUsuario): Future[Validation[PersistenceException, Int]] = {
    val repo = new UsuariosRepository()
    repo.guardarPinUsuario(pinUsuario)
  }

  private def transformValidationList(origin: Validation[PersistenceException, List[eUsuario]]): Validation[PersistenceException, List[Usuario]] = {
    origin match {
      case zSuccess(response: List[eUsuario]) => zSuccess(DataAccessTranslator.translateUsuario(response))
      case zFailure(error)    =>  zFailure(error)
    }
  }

  private def transformValidation(origin: Validation[PersistenceException, Option[eUsuario]]): Validation[PersistenceException, Option[Usuario]] = {
    origin match {
      case zSuccess(response) =>
        response match {
          case Some(usuario) => zSuccess(Some(DataAccessTranslator.translateUsuario(usuario)))
          case _ => zSuccess(None)
        }

      case zFailure(error)    =>  zFailure(error)
    }
  }

  private def transformValidationUsuarioEmpresarial(origin: Validation[PersistenceException, Option[eUsuarioEmpresarial]]): Validation[PersistenceException, Option[UsuarioEmpresarial]] = {
    origin match {
      case zSuccess(response) =>
        response match {
          case Some(usuario) => zSuccess(Some(DataAccessTranslator.translateUsuarioEmpresarial(usuario)))
          case _ => zSuccess(None)
        }

      case zFailure(error)    =>  zFailure(error)
    }
  }



}


