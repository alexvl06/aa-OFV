package co.com.alianza.persistence.repositories

import java.sql.Timestamp
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.persistence.entities.{ CustomDriver, UsuarioEmpresarial, UsuarioEmpresarialTable, EmpresaTable, UsuarioEmpresarialEmpresaTable }
import enumerations.EstadosEmpresaEnum
import scala.util.Try
import scalaz.Validation
import scala.concurrent.{ ExecutionContext, Future }
import co.com.alianza.persistence.entities._
import CustomDriver.simple._

/**
 * Created by s4n on 2014
 */
//Todo : Borrrar ! Ya esta en el refactor By : Alexa
class UsuariosEmpresarialRepository(implicit executionContext: ExecutionContext) extends AlianzaRepository {

  val empresas = TableQuery[EmpresaTable]
  val usuariosEmpresariales = TableQuery[UsuarioEmpresarialTable]
  val usuariosEmpresarialesAdmin = TableQuery[UsuarioEmpresarialAdminTable]
  val usuariosEmpresarialesEmpresa = TableQuery[UsuarioEmpresarialEmpresaTable]
  val usuariosEmpresarialesAdminEmpresa = TableQuery[UsuarioEmpresarialAdminEmpresaTable]
  val pinempresa = TableQuery[PinEmpresaTable]

  // ---------------------- ALIANZA DAO ----------------------------------

  def obtieneUsuarioEmpresaPorNitYUsuario(nit: String, usuario: String): Future[Validation[PersistenceException, Option[UsuarioEmpresarial]]] = loan {
    implicit session =>
      val resultTry = session.database.run(
        (
        for {
          ((usuarioEmpresarial, usuarioEmpresarialEmpresa), empresa) <- usuariosEmpresariales join usuariosEmpresarialesEmpresa on {
            (ue, uee) => ue.id === uee.idUsuarioEmpresarial && ue.usuario === usuario
          } join empresas on {
            case ((ue, uee), e) => e.nit === nit && uee.idEmpresa === e.id
          }
        } yield (usuarioEmpresarial)
      ).result.headOption
      )

      resolveTry(resultTry, "Consulta usuario empresarial por nit y usuario")
  }

  def obtieneUsuarioEmpresaAdminPorNitYUsuario(nit: String, usuario: String): Future[Validation[PersistenceException, Option[UsuarioEmpresarialAdmin]]] = loan {
    implicit session =>
      val resultTry = session.database.run(
        (
        for {
          ((usuarioEmpresarial, usuarioEmpresarialEmpresa), empresa) <- usuariosEmpresarialesAdmin join usuariosEmpresarialesAdminEmpresa on {
            (ue, uee) => ue.id === uee.idUsuarioEmpresarialAdmin && ue.usuario === usuario
          } join empresas on {
            case ((ue, uee), e) => e.nit === nit && uee.idEmpresa === e.id
          }
        } yield usuarioEmpresarial
      ).result.headOption
      )

      resolveTry(resultTry, "Obtiene id agente empresarial de acuerdo a los 3 paramteros dados")
  }

  //Obtengo como resultado una tupla que me devuelve el usuarioEmpresarial junto con el estado de la empresa
  def obtenerUsuarioToken(token: String): Future[Validation[PersistenceException, Option[(UsuarioEmpresarial, Int)]]] = loan {
    implicit session =>
      val query = for {
        (agenteEmpresarial, empresa) <- usuariosEmpresariales join usuariosEmpresarialesEmpresa on {
          (ue, uee) => ue.token === token && ue.id === uee.idUsuarioEmpresarial
        } join empresas on {
          case ((ue, uee), e) => uee.idEmpresa === e.id
        }
      } yield {
        (agenteEmpresarial._1, empresa.estadoEmpresa)
      }

      val resultTry = session.database.run(query.result.headOption)
      resolveTry(resultTry, "Consulta usuario empresarial por token: " + token)
  }

  def validacionAgenteEmpresarial(numIdentificacionAgenteEmpresarial: String, correoUsuarioAgenteEmpresarial: String, tipoIdentiAgenteEmpresarial: Int, idClienteAdmin: Int): Future[Validation[PersistenceException, Option[(Int, Int)]]] = loan {
    implicit session =>
      val query =
        (for {
          (clienteAdministrador, agenteEmpresarial) <- usuariosEmpresarialesAdmin join usuariosEmpresarialesAdminEmpresa on {
            (uea, ueae) => uea.id === ueae.idUsuarioEmpresarialAdmin && uea.id === idClienteAdmin
          } join usuariosEmpresarialesEmpresa on {
            case ((uea, ueae), uee) => ueae.idEmpresa === uee.idEmpresa
          } join usuariosEmpresariales on {
            case (((uea, ueae), uee), ae) => uee.idUsuarioEmpresarial === ae.id && ae.identificacion === numIdentificacionAgenteEmpresarial && ae.correo === correoUsuarioAgenteEmpresarial && ae.tipoIdentificacion === tipoIdentiAgenteEmpresarial
          }
        } yield (agenteEmpresarial)).result.headOption

      val resultTry = session.database.run(query)

      val resultIdUsuarioAE: Future[Option[(Int, Int)]] = resultTry map {
        x =>
          x match {
            case None => None
            case Some(x) =>
              Some((x.id, x.estado))
          }
      }
      resolveTry(resultIdUsuarioAE, "Obtiene id agente empresarial de acuerdo a los 3 parametros dados")
  }

  // ---------------------------------- EMPRESA ---------------------------------------------------------------

  def obtenerEmpresaPorNit(nit: String): Future[Validation[PersistenceException, Option[Empresa]]] = loan {
    implicit session =>
      val resultTry = session.database.run((empresas.filter(_.nit === nit).result.headOption))
      resolveTry(resultTry, "Agregar ips agente empresarial")
  }

  //------------------------------------ USUARIOS EMPRESARIALES ADMIN ----------------------------------------------

  def obtenerUsuarioEmpresarialAdminPorId(idUsuario: Int): Future[Validation[PersistenceException, Option[UsuarioEmpresarialAdmin]]] = loan {
    implicit session =>
      val resultTry = session.database.run(usuariosEmpresarialesAdmin.filter(_.id === idUsuario).result.headOption)
      resolveTry(resultTry, "Obtener agente empresarial admin por ID ")
  }

  //------------------------------------ USUARIOS EMPRESARIALES  ----------------------------------------------

  def AcambiarEstadoAgenteEmpresarial(idUsuarioAgenteEmpresarial: Int, estado: EstadosEmpresaEnum.estadoEmpresa): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val query = for { u <- usuariosEmpresariales if u.id === idUsuarioAgenteEmpresarial } yield u.estado

      val resultTry = session.database.run(query.update(estado.id))
      resolveTry(resultTry, "Cambiar Estado Usuario Agente Empresarial")
  }

  def cambiarBloqueoDesbloqueoAgenteEmpresarial(idUsuarioAgenteEmpresarial: Int, estado: EstadosEmpresaEnum.estadoEmpresa, timestamp: Timestamp): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val query = for { u <- usuariosEmpresariales if u.id === idUsuarioAgenteEmpresarial } yield (u.estado, u.fechaActualizacion)
      val resultTry = session.database.run(query.update(estado.id, timestamp))
      resolveTry(resultTry, "Cambiar Estado Usuario Agente Empresarial")
  }

  def insertarAgenteEmpresarial(agenteEmpresarial: UsuarioEmpresarial): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val resultTry = session.database.run((usuariosEmpresariales returning usuariosEmpresariales.map(_.id)) += agenteEmpresarial)
      resolveTry(resultTry, "Agregar agente empresarial")
  }

  def obtenerUsuarioEmpresarialPorId(idUsuario: Int): Future[Validation[PersistenceException, Option[UsuarioEmpresarial]]] = loan {
    implicit session =>
      val resultTry = session.database.run(usuariosEmpresariales.filter(_.id === idUsuario).result.headOption)
      resolveTry(resultTry, "Obtener agente empresarial por ID ")
  }

  def existeUsuario(idUsuario: Int, nit: String, usuario: String): Future[Validation[PersistenceException, Boolean]] = loan {
    implicit session =>
      val resultTry = session.database.run(usuariosEmpresariales.filter(usu => usu.usuario === usuario && usu.identificacion === nit && usu.id =!= idUsuario).exists.result)
      resolveTry(resultTry, "Existe agente con mismo usuario pero diferente id ?")
  }

  def asociarTokenUsuario(usuarioId: Int, token: String): Future[Validation[PersistenceException, Int]] = loan {

    implicit session =>
      val resultTry = session.database.run(usuariosEmpresariales.filter(_.id === usuarioId).map(_.token).update(Some(token)))
      resolveTry(resultTry, "Actualizar token de usuario empresarial")
  }

  def invalidarTokenUsuario(token: String): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val resultTry = session.database.run(usuariosEmpresariales.filter(_.token === token).map(_.token).update(Some(null)))
      resolveTry(resultTry, "Invalidar token usuario")
  }

  def obtenerUsuarioPorToken(token: String): Future[Validation[PersistenceException, Option[UsuarioEmpresarial]]] = loan {
    implicit session =>
      val resultTry = session.database.run(usuariosEmpresariales.filter(_.token === token).result.headOption)
      resolveTry(resultTry, "Consulta usuario empresarial con token" + token)
  }

  def actualizarNumeroIngresosErroneos(idUsuario: Int, numeroIntentos: Int): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val resultTry = session.database.run(usuariosEmpresariales.filter(_.id === idUsuario).map(_.numeroIngresosErroneos).update(numeroIntentos))
      resolveTry(resultTry, "Actualizar usuario empresarial admin en numeroIngresosErroneos ")
  }

  def actualizarIpUltimoIngreso(idUsuario: Int, ipActual: String): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val resultTry = session.database.run(usuariosEmpresariales.filter(_.id === idUsuario).map(_.ipUltimoIngreso).update(Some(ipActual)))
      resolveTry(resultTry, "Actualizar usuario empresarial admin en ipUltimoIngreso ")
  }

  def actualizarFechaUltimoIngreso(idUsuario: Int, fechaActual: Timestamp): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val resultTry = session.database.run(usuariosEmpresariales.filter(_.id === idUsuario).map(_.fechaUltimoIngreso).update(Some(fechaActual)))
      resolveTry(resultTry, "Actualizar usuario empresarial admin en fechaUltimoIngreso ")
  }

  def obtenerUsuarioEmpresarialAgentePorId(idUsuario: Int): Future[Validation[PersistenceException, Option[UsuarioEmpresarial]]] = loan {
    implicit session =>
      val resultTry = session.database.run(usuariosEmpresariales.filter(_.id === idUsuario).result.headOption)
      resolveTry(resultTry, "Actualizar usuario empresarial admin en fechaUltimoIngreso ")
  }

  def actualizarEstadoUsuario(idUsuario: Int, estado: Int): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val resultTry = session.database.run(usuariosEmpresariales.filter(_.id === idUsuario).map(_.estado).update(estado))
      resolveTry(resultTry, "Actualizar estado de usuario empresarial agente")
  }

  /**
   * Actualizar datos agente empresarial
   * @param id
   * @param usuario
   * @param correo
   * @param nombreUsuario
   * @param cargo
   * @param descripcion
   * @return
   */
  def actualizarAgente(id: Int, usuario: String, correo: String, nombreUsuario: String, cargo: String, descripcion: Option[String]): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val query = usuariosEmpresariales.filter(_.id === id).map(a => (a.correo, a.usuario, a.nombreUsuario, a.cargo))
      val resultTry = session.database.run(query.update(correo, usuario, nombreUsuario, cargo))
      resolveTry(resultTry, "Actualizar usuario empresarial agente")
  }

  //--------------------------------------- USUARIO EMPRESARIAL EMPRESA  ---------------------------------------------------

  def asociarAgenteEmpresarialConEmpresa(usuarioEmpresarialEmpresa: UsuarioEmpresarialEmpresa) = loan {
    implicit session =>
      val resultTry = session.database.run(usuariosEmpresarialesEmpresa += usuarioEmpresarialEmpresa)
      resolveTry(resultTry, "Asociar usuario agente empresarial a la empresa")
  }

  //------------------------------------------- PIN EMPRESA  ----------------------------------------------------

  def guardarPinEmpresaAgenteEmpresarial(pinEmpresaAgenteEmpresarial: PinEmpresa): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val resultTry = session.database.run((pinempresa += pinEmpresaAgenteEmpresarial))
      resolveTry(resultTry, "Agregar pin empresa del agente empresarial")
  }

  def eliminarPinEmpresaReiniciarAnteriores(idUsuarioAgenteEmpresarial: Int, usoPinEmpresa: Int): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val resultTry = session.database.run(pinempresa.filter(x => x.idUsuarioEmpresarial === idUsuarioAgenteEmpresarial && x.uso === usoPinEmpresa).delete)
      resolveTry(resultTry, "Eliminar pin(es) empresa anteriores del agente empresarial asociado cuando el uso es reiniciar contrasena")
  }

  //------------------------------------------------------------------------------------------------------------

}
