package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.config.DBConfig
import co.com.alianza.persistence.entities._
import slick.lifted.TableQuery

import scala.concurrent.Future

case class AlianzaDAO()(implicit dcConfig: DBConfig) extends AlianzaDAOs {

  val recursos = TableQuery[RecursoPerfilTable]
  val usuarios = TableQuery[UsuarioTable]
  val perfilesUsuario = TableQuery[PerfilUsuarioTable]
  val usuariosEmpresarialesAdmin = TableQuery[UsuarioEmpresarialAdminTable]
  val usuariosEmpresarialesAdminEmpresa = TableQuery[UsuarioEmpresarialAdminEmpresaTable]
  val pinusuariosEmpresarialesAdmin = TableQuery[PinUsuarioEmpresarialAdminTable]
  val empresas = TableQuery[EmpresaTable]

  import dcConfig.db._
  import dcConfig.profile.api._

  /**
   * Obtiene los recursos relacionados a los perfiles del usuario
   *
   * @param idUsuario Id del usuario para obtener los recursos
   * @return
   */
  def getResources(idUsuario: Int): Future[Seq[RecursoPerfil]] = {

      val usuariosRecursosJoin = for {
        ((usu: UsuarioTable, per: PerfilUsuarioTable), rec: RecursoPerfilTable) <-
        usuarios join perfilesUsuario on (_.id === _.idUsuario) join recursos on (_._2.idPerfil === _.idPerfil) if usu.id === idUsuario
      } yield rec

      run(usuariosRecursosJoin.result)
  }

  /**
   * Obtiene el token de usuario empresarial admin
   *
   * @param token Token del admin empresarial que se desea validar
   * @return
   */
  def getAdminToken(token: String): Future[Option[(UsuarioEmpresarialAdmin, Int)]] = {

      val query = for {
        (clienteAdministrador, empresa) <- usuariosEmpresarialesAdmin join usuariosEmpresarialesAdminEmpresa on {
          (uea, ueae) => uea.token === token && uea.id === ueae.idUsuarioEmpresarialAdmin
        } join empresas on {
          case ((uea, ueae), e) => ueae.idEmpresa === e.id
        }
      } yield {
        (clienteAdministrador._1, empresa.estadoEmpresa)
      }
      run(query.result.headOption)
  }

  /**
   * Crea el Pin para el envio de correos de admin empresarial
   *
   * @param pinUsuario Pin
   * @return
   */
  def createPinAdmin(pinUsuario: PinUsuarioEmpresarialAdmin): Future[Int] = {
      run((pinusuariosEmpresarialesAdmin returning pinusuariosEmpresarialesAdmin.map(_.id.get)) += pinUsuario)
  }


}
