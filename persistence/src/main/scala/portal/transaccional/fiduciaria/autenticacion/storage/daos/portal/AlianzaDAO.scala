package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities._
import portal.transaccional.fiduciaria.autenticacion.storage.config.DBConfig
import slick.lifted.TableQuery

import scala.concurrent.Future

case class AlianzaDAO()(implicit dcConfig: DBConfig) extends AlianzaDAOs {

  val recursos = TableQuery[RecursoPerfilTable]
  val usuarios = TableQuery[UsuarioTable]
  val perfilesUsuario = TableQuery[PerfilUsuarioTable]
  val usuariosEmpresarialesAdmin = TableQuery[UsuarioEmpresarialAdminTable]
  val usuariosEmpresarialesAdminEmpresa = TableQuery[UsuarioEmpresarialAdminEmpresaTable]
  val empresas = TableQuery[EmpresaTable]
  val usuariosEmpresariales = TableQuery[UsuarioEmpresarialTable]
  val usuariosEmpresarialesEmpresa = TableQuery[UsuarioEmpresarialEmpresaTable]
  val pinempresa = TableQuery[PinEmpresaTable]
  val recursosPerfilesAgentes = TableQuery[RecursoPerfilAgenteTable]
  val perfilesAgentes = TableQuery[PerfilAgenteAgenteTable]
  val perfilesClientesAdmin = TableQuery[PerfilClienteAdminClienteAdminTable]
  val recursosPerfilesAdmin = TableQuery[RecursoPerfilClienteAdminTable]
  val preguntasTable = TableQuery[PreguntasAutovalidacionTable]
  val respuestasUsuarioTable = TableQuery[RespuestasAutovalidacionUsuarioTable]
  val respuestasClienteAdministradorTable = TableQuery[RespuestasAutovalidacionUsuarioAdministradorTable]

  import dcConfig.DB._
  import dcConfig.driver.api._

  //  ------------------  Recurso ---------------------------
  /**
   * Obtiene los recursos relacionados a los perfiles del usuario
   *
   * @param idUsuario Id del usuario para obtener los recursos
   * @return
   */

  def getResources(idUsuario: Int): Future[Seq[RecursoPerfil]] = {
    val usuariosRecursosJoin = for {
      ((usu: UsuarioTable, per: PerfilUsuarioTable), rec: RecursoPerfilTable) <- usuarios join perfilesUsuario on (_.id === _.idUsuario) join recursos on
        (_._2.idPerfil === _.idPerfil) if usu.id === idUsuario
    } yield rec
    run(usuariosRecursosJoin.result)
  }

  def getAgenteResources(idUsuario: Int): Future[Seq[RecursoPerfilAgente]] = {
    val resources = for {
      ((usu, per), rec) <- usuariosEmpresariales join perfilesAgentes on (_.id === _.idUsuario) join recursosPerfilesAgentes on
        (_._2.idPerfil === _.idPerfil) if usu.id === idUsuario
    } yield rec
    run(resources.result)
  }

  def getAdminResources(idUsuario: Int): Future[Seq[RecursoPerfilClienteAdmin]] = {
    val resources = for {
      ((usu, per), rec) <- usuariosEmpresarialesAdmin join perfilesClientesAdmin on (_.id === _.idUsuario) join recursosPerfilesAdmin on
        (_._2.idPerfil === _.idPerfil) if usu.id === idUsuario
    } yield rec
    run(resources.result)
  }

  //  --------------- Usuario empresarial -------------------
  /**
   * Obtiene el token de usuario empresarial admin
   *
   * @param token Token del admin empresarial que se desea validar
   * @return
   */
  def getAdminTokenAgente(token: String): Future[Option[(UsuarioEmpresarialAdmin, Int)]] = {
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

  def getByNitAndUserAgente(nit: String, usuario: String): Future[Option[UsuarioEmpresarial]] = {
    run(
      (for {
      ((usuarioEmpresarial, usuarioEmpresarialEmpresa), empresa) <- usuariosEmpresariales join usuariosEmpresarialesEmpresa on {
        (ue, uee) => ue.id === uee.idUsuarioEmpresarial && ue.usuario === usuario
      } join empresas on {
        case ((ue, uee), e) => e.nit === nit && uee.idEmpresa === e.id
      }
    } yield usuarioEmpresarial).result.headOption
    )
  }

  //Obtengo como resultado una tupla que me devuelve el usuarioEmpresarial junto con el estado de la empresa
  def getByTokenAgente(token: String): Future[(UsuarioEmpresarial, Int)] = {
    val query =
      for {
        (agenteEmpresarial, empresa) <- usuariosEmpresariales join usuariosEmpresarialesEmpresa on {
          (ue, uee) => ue.token === token && ue.id === uee.idUsuarioEmpresarial
        } join empresas on {
          case ((ue, uee), e) => uee.idEmpresa === e.id
        }
      } yield (agenteEmpresarial._1, empresa.estadoEmpresa)

    run(query.result.head)
  }

  def validateAgente(id: String, correo: String, tipoId: Int, idClienteAdmin: Int): Future[Option[UsuarioEmpresarial]] = {
    val query =
      for {
        (clienteAdministrador, agenteEmpresarial) <- usuariosEmpresarialesAdmin join usuariosEmpresarialesAdminEmpresa on {
          (uea, ueae) => uea.id === ueae.idUsuarioEmpresarialAdmin && uea.id === idClienteAdmin
        } join usuariosEmpresarialesEmpresa on {
          case ((uea, ueae), uee) => ueae.idEmpresa === uee.idEmpresa
        } join usuariosEmpresariales on {
          case (((uea, ueae), uee), ae) =>
            uee.idUsuarioEmpresarial === ae.id && ae.identificacion === id && ae.correo === correo &&
              ae.tipoIdentificacion === tipoId
        }
      } yield agenteEmpresarial

    run(query.result.headOption)

    //Todo : Esta logica debe ir en un Repo  By :Alexa
    //      resultTry.map (
    //        x => x match {
    //          case None => None
    //          case Some(x) => Some((x.id, x.estado))
    //        }
    //      )
  }

  // ------------------- Admin Empresarial --------------------

  def getByNitAndUserAdmin(nit: String, usuario: String): Future[Option[UsuarioEmpresarialAdmin]] = {
    run(
      (for {
      ((usuarioEmpresarial, usuarioEmpresarialEmpresa), empresa) <- usuariosEmpresarialesAdmin join usuariosEmpresarialesAdminEmpresa on {
        (ue, uee) => ue.id === uee.idUsuarioEmpresarialAdmin && ue.usuario === usuario
      } join empresas on {
        case ((ue, uee), e) => e.nit === nit && uee.idEmpresa === e.id
      }
    } yield usuarioEmpresarial).result.headOption
    )
  }

  //Obtengo como resultado una tupla que me devuelve el usuarioEmpresarialAdmin junto con el estado de la empresa
  def getByTokenAdmin(token: String): Future[(UsuarioEmpresarialAdmin, Int)] = {
    val query =
      for {
        (agenteEmpresarialAdmin, empresa) <- usuariosEmpresarialesAdmin join usuariosEmpresarialesAdminEmpresa on {
          (ue, uee) => ue.token === token && ue.id === uee.idUsuarioEmpresarialAdmin
        } join empresas on {
          case ((ue, uee), e) => uee.idEmpresa === e.id
        }
      } yield (agenteEmpresarialAdmin._1, empresa.estadoEmpresa)
    run(query.result.head)
  }

  //  ------------------  Preguntas ---------------------------

  def getIndividualClientQuestions(idUsuario: Int): Future[Seq[(PreguntaAutovalidacion, RespuestasAutovalidacionUsuario)]] = {
    val query = for {
      (pregunta, respuesta) <- preguntasTable join respuestasUsuarioTable on (_.id === _.idPregunta)
      if respuesta.idUsuario === idUsuario
    } yield (pregunta, respuesta)
    run(query.result)
  }

  def getAdministratorClientQuestions(idUsuario: Int): Future[Seq[(PreguntaAutovalidacion, RespuestasAutovalidacionUsuario)]] = {
    val query = for {
      (pregunta, respuesta) <- preguntasTable join respuestasClienteAdministradorTable on (_.id === _.idPregunta)
      if respuesta.idUsuario === idUsuario
    } yield (pregunta, respuesta)
    run(query.result)
  }

  def deleteIndividualClientAnswers(idUsuario: Int): Future[Int] = {
    run(respuestasUsuarioTable.filter(x => x.idUsuario === idUsuario).delete)
  }

  def bloquearRespuestasClienteAdministrador(idUsuario: Int): Future[Int] = {
    run(respuestasClienteAdministradorTable.filter(x => x.idUsuario === idUsuario).delete)
  }
}
