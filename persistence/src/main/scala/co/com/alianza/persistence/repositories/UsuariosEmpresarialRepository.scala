package co.com.alianza.persistence.repositories

import java.sql.Timestamp

import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.persistence.entities.{CustomDriver, UsuarioEmpresarial, UsuarioEmpresarialTable, EmpresaTable, UsuarioEmpresarialEmpresaTable}
import enumerations.EstadosEmpresaEnum
import scala.util.Try
import scalaz.Validation
import scala.concurrent.{ExecutionContext, Future}
import co.com.alianza.persistence.entities._
import CustomDriver.simple._

/**
 * Created by manuel on 9/12/14.
 */
class UsuariosEmpresarialRepository(implicit executionContext: ExecutionContext) extends AlianzaRepository {

  val Empresas = TableQuery[EmpresaTable]
  val UsuariosEmpresariales = TableQuery[UsuarioEmpresarialTable]
  val UsuariosEmpresarialesAdmin = TableQuery[UsuarioEmpresarialAdminTable]
  val UsuariosEmpresarialesEmpresa = TableQuery[UsuarioEmpresarialEmpresaTable]
  val UsuariosEmpresarialesAdminEmpresa = TableQuery[UsuarioEmpresarialAdminEmpresaTable]
  val pinempresa = TableQuery[PinEmpresaTable]

  def obtieneUsuarioEmpresaPorNitYUsuario(nit: String, usuario: String): Future[Validation[PersistenceException, Option[UsuarioEmpresarial]]] = loan {
    implicit session => resolveTry(Try {
      (
        for {
          ((usuarioEmpresarial, usuarioEmpresarialEmpresa), empresa) <-
          UsuariosEmpresariales join UsuariosEmpresarialesEmpresa on {
            (ue, uee) => ue.id === uee.idUsuarioEmpresarial  && ue.usuario === usuario
          } join Empresas on {
            case ((ue, uee), e) => e.nit === nit && uee.idEmpresa === e.id
          }
        } yield (usuarioEmpresarial)
        ) firstOption
    }, "Consulta usuario empresarial por nit y usuario")
  }

  def obtieneUsuarioEmpresaAdminPorNitYUsuario(nit: String, usuario: String): Future[Validation[PersistenceException, Option[UsuarioEmpresarialAdmin]]] = loan {
    implicit session => resolveTry(Try {
      (
        for {
          ((usuarioEmpresarial, usuarioEmpresarialEmpresa), empresa) <-
          UsuariosEmpresarialesAdmin join UsuariosEmpresarialesAdminEmpresa on {
            (ue, uee) => ue.id === uee.idUsuarioEmpresarialAdmin && ue.usuario === usuario
          } join Empresas on {
            case ((ue, uee), e) => e.nit === nit  && uee.idEmpresa === e.id
          }
        } yield (usuarioEmpresarial)
        ) firstOption
    }, "Consulta usuario empresarial administrador por nit y usuario")
  }

  def obtieneUsuarioEmpresaAdminPorNit(nit: String): Future[Validation[PersistenceException, Option[UsuarioEmpresarialAdmin]]] = loan {
    implicit session => resolveTry(Try {
      (
        for {
          ((usuarioEmpresarial, usuarioEmpresarialEmpresa), empresa) <-
          UsuariosEmpresarialesAdmin join UsuariosEmpresarialesAdminEmpresa on {
            (ue, uee) => ue.id === uee.idUsuarioEmpresarialAdmin
          } join Empresas on {
            case ((ue, uee), e) => e.nit === nit  && uee.idEmpresa === e.id
          }
        } yield (usuarioEmpresarial)
        ) firstOption
    }, "Consulta usuario empresarial administrador por nit y usuario")
  }

  def asociarTokenUsuario(usuarioId: Int, token: String): Future[Validation[PersistenceException, Int]] = loan {

    implicit session =>
      val resultTry = Try {
        UsuariosEmpresariales.filter(_.id === usuarioId).map(_.token).update(Some(token))
      }
      resolveTry(resultTry, "Actualizar token de usuario empresarial")
  }

  def validacionAgenteEmpresarial(numIdentificacionAgenteEmpresarial: String, correoUsuarioAgenteEmpresarial: String, tipoIdentiAgenteEmpresarial: Int, idClienteAdmin: Int): Future[Validation[PersistenceException, Option[(Int, Int)]]] = loan {
    implicit session =>
      val resultTry = Try {
        (for {
          (clienteAdministrador, agenteEmpresarial) <-
          UsuariosEmpresarialesAdmin join UsuariosEmpresarialesAdminEmpresa on {
            (uea, ueae) => uea.id === ueae.idUsuarioEmpresarialAdmin && uea.id === idClienteAdmin
          } join UsuariosEmpresarialesEmpresa on {
            case ((uea, ueae), uee) => ueae.idEmpresa === uee.idEmpresa
          } join UsuariosEmpresariales on {
            case (((uea, ueae), uee), ae) => uee.idUsuarioEmpresarial === ae.id && ae.identificacion === numIdentificacionAgenteEmpresarial && ae.correo === correoUsuarioAgenteEmpresarial && ae.tipoIdentificacion === tipoIdentiAgenteEmpresarial
          }
        } yield (agenteEmpresarial)
        ).list.headOption
      }

      val resultIdUsuarioAE: Try[Option[(Int, Int)]] = resultTry map {
         x => x match {
           case None => None
           case Some(x) =>
             Some((x.id, x.estado))
         }
      }
      resolveTry(resultIdUsuarioAE, "Obtiene id agente empresarial de acuerdo a los 3 paramteros dados")
  }

  def CambiarEstadoAgenteEmpresarial(idUsuarioAgenteEmpresarial: Int, estado: EstadosEmpresaEnum.estadoEmpresa): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val query = for {u <- UsuariosEmpresariales if u.id === idUsuarioAgenteEmpresarial} yield u.estado
      val resultTry = Try {
        query.update(estado.id)
      }
      resolveTry(resultTry, "Cambiar Estado Usuario Agente Empresarial")
  }

  def obtenerUsuarioToken(token: String): Future[Validation[PersistenceException, Option[UsuarioEmpresarial]]] = loan {
    implicit session =>
      val resultTry = Try {
        UsuariosEmpresariales.filter(_.token === token).list.headOption
      }
      resolveTry(resultTry, "Consulta usuario empresarial por token: " + token)
  }

  def guardarPinEmpresaAgenteEmpresarial(pinEmpresaAgenteEmpresarial: PinEmpresa): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val resultTry = Try {
        (pinempresa += pinEmpresaAgenteEmpresarial)
      }
      resolveTry(resultTry, "Agregar pin empresa del agente empresarial")
  }

  def eliminarPinEmpresaReiniciarAnteriores(idUsuarioAgenteEmpresarial: Int, usoPinEmpresa: Int): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val resultTry = Try {
        pinempresa.filter(x => x.idUsuarioEmpresarial === idUsuarioAgenteEmpresarial && x.uso === usoPinEmpresa).delete
      }
      resolveTry(resultTry, "Eliminar pin(es) empresa anteriores del agente empresarial asociado cuando el uso es reiniciar contrasena")
  }

  def insertarAgenteEmpresarial(agenteEmpresarial : UsuarioEmpresarial): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val resultTry = Try{ (UsuariosEmpresariales  returning UsuariosEmpresariales.map(_.id)) += agenteEmpresarial }
      resolveTry(resultTry, "Agregar agente empresarial")
  }

  def obtenerEmpresaPorNit(nit : String) : Future[Validation[PersistenceException, Option[Empresa]]] = loan {
    implicit session =>
      val resultTry = Try{ (Empresas.filter(_.nit === nit).list.headOption ) }
      resolveTry(resultTry, "Agregar ips agente empresarial")
  }

  def asociarAgenteEmpresarialConEmpresa(usuarioEmpresarialEmpresa: UsuarioEmpresarialEmpresa) = loan {
    implicit session =>
      val resultTry = Try{ (UsuariosEmpresarialesEmpresa += usuarioEmpresarialEmpresa) }
      resolveTry(resultTry, "Asociar usuario agente empresarial a la empresa")
  }

  def actualizarNumeroIngresosErroneos( idUsuario:Int, numeroIntentos:Int ): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val resultTry = Try{ UsuariosEmpresariales.filter( _.id === idUsuario ).map(_.numeroIngresosErroneos).update(numeroIntentos )  }
      resolveTry(resultTry, "Actualizar usuario empresarial admin en numeroIngresosErroneos ")
  }

  def actualizarIpUltimoIngreso( idUsuario:Int, ipActual:String ): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val resultTry = Try{ UsuariosEmpresariales.filter( _.id === idUsuario ).map(_.ipUltimoIngreso ).update(Some(ipActual))  }
      resolveTry(resultTry, "Actualizar usuario empresarial admin en ipUltimoIngreso ")
  }

  def actualizarFechaUltimoIngreso( idUsuario:Int, fechaActual : Timestamp ): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val resultTry = Try{ UsuariosEmpresariales.filter( _.id === idUsuario ).map(_.fechaUltimoIngreso ).update(Some(fechaActual))  }
      resolveTry(resultTry, "Actualizar usuario empresarial admin en fechaUltimoIngreso ")
  }

  def obtenerUsuarioEmpresarialAgentePorId(idUsuario: Int): Future[Validation[PersistenceException, Option[UsuarioEmpresarial]]] = loan {
    implicit session =>
      val resultTry = Try{ UsuariosEmpresariales.filter( _.id === idUsuario ).firstOption  }
      resolveTry(resultTry, "Actualizar usuario empresarial admin en fechaUltimoIngreso ")
  }

  def actualizarEstadoUsuario( idUsuario:Int, estado:Int ): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val resultTry = Try{ UsuariosEmpresariales.filter( _.id === idUsuario ).map(_.estado ).update(estado)  }
      resolveTry(resultTry, "Actualizar estado de usuario empresarial agente")
  }

}
