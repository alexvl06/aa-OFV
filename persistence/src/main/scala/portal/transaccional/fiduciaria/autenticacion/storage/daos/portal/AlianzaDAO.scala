package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities._
import enumerations.PerfilInmobiliarioEnum
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
  val usuariosAgentesInmobiliarios = TableQuery[UsuarioAgenteInmobiliarioTable]
  val permisosInmobiliarios = TableQuery[PermisoInmobiliarioTable]
  val recursosGraficosInmobiliarios = TableQuery[RecursoGraficoInmobiliarioTable]
  val recursosInmobiliarios = TableQuery[RecursoInmobiliarioTable]
  val recursosBackendInmobiliarios = TableQuery[RecursoBackendInmobiliarioTable]
  val perfilInmobiliario = TableQuery[PerfilInmobiliarioTable]
  val recursosPerfilInmobiliario = TableQuery[RecursoPerfilInmobiliarioTable]

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

  def getClienteIndividualResources(): Future[Seq[RecursoPerfil]] = {
    run(this.recursos.result)
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

  //  ------------------  Agente inmobiliario ---------------------------

  def getPermisosProyectoInmobiliario(nit: String, idFideicomiso: Int, idProyecto: Int): Future[Seq[PermisoAgenteInmobiliario]] = {
    val query = for {
      (agentes, permisos) <- usuariosAgentesInmobiliarios join permisosInmobiliarios on (_.id === _.idAgente)
      if agentes.identificacion === nit && permisos.fideicomiso === idFideicomiso && permisos.proyecto === idProyecto
    } yield permisos
    run(query.result)
  }

  def getPermisosProyectoInmobiliario(nit: String, idFideicomiso: Int, idProyecto: Int, idAgentes: Seq[Int]): Future[Seq[PermisoAgenteInmobiliario]] = {
    val query = for {
      agentesFiltrados <- usuariosAgentesInmobiliarios.filter(_.id inSetBind idAgentes)
      permisos <- permisosInmobiliarios if agentesFiltrados.id === permisos.idAgente
      if agentesFiltrados.identificacion === nit && permisos.fideicomiso === idFideicomiso && permisos.proyecto === idProyecto
    } yield permisos
    run(query.result)
  }

  // Obtiene los permisos a los que un agente puede acceder
  def getPermisosProyectoInmobiliarioByAgente(username: String, idAgente: Int): Future[Seq[PermisoAgenteInmobiliario]] = {
    val query = for {
      a <- usuariosAgentesInmobiliarios if a.id === idAgente if a.usuario === username
      p <- permisosInmobiliarios if p.idAgente === a.id
    } yield p
    run(query.result)
  }

  // Obtiene el menu de constructor o agente
  def getAdminResourcesVisible(tipoPermisos: String): Future[Seq[RecursoGraficoInmobiliario]] = {
    val query = getGraphicalResources(tipoPermisos)
    run(query.result)
  }

  // Obtiene su propio menu
  def getAgentResourcesById(idAgente: Int): Future[Seq[RecursoGraficoInmobiliario]] = {
    val query1 = getAgentPermission(idAgente)
    val query2 =
      for {
        x <- query1.filterNot(_.visible)
        y <- recursosGraficosInmobiliarios.filter(_.id === x.menuPrincipal)
      } yield y

    val query = query1 ++ query2
    run(query.distinctOn(_.id).result)
  }

  //Obtiene los recursos a los que puede acceder Admin
  def getMenuAdmin(isInterno: Boolean): Future[Seq[RecursoBackendInmobiliario]] = {
    val tipoAdmin = if (isInterno) PerfilInmobiliarioEnum.agenteInterno.toString else PerfilInmobiliarioEnum.admin.toString
    val query = for {
      g <- getGraphicalResources(tipoAdmin)
      r <- getBackendResources(g)
    } yield r
    run(query.result)
  }

  def getByTokenAgenteInmobiliario(token: String): Future[UsuarioAgenteInmobiliario] = {
    val query = usuariosAgentesInmobiliarios.filter(a => a.token === token)
    run(query.result.head)
  }

  //Obtiene los recursos a los que puede acceder Agente
  def getMenuAgenteInmob(idAgente: Int): Future[Seq[RecursoBackendInmobiliario]] = {
    val query = for {
      g <- getAgentPermission(idAgente)
      r <- getBackendResources(g)
    } yield r

    val query2 = for {
      g <- getGraphicalResources(PerfilInmobiliarioEnum.agente.toString).filterNot(_.visible)
      r <- getBackendResources(g)
    } yield r
    run((query ++ query2).distinctOn(_.id).result)
  }

  private def getGraphicalResources(tipo: String) = {
    for {
      p <- perfilInmobiliario if p.nombre === tipo
      pg <- recursosPerfilInmobiliario if p.id === pg.idPerfil
      g <- recursosGraficosInmobiliarios if g.id === pg.urlRecurso
    } yield g
  }

  private def getBackendResources(table: RecursoGraficoInmobiliarioTable) = {
    for {
      rg <- recursosInmobiliarios if rg.idGrafico === table.id
      r <- recursosBackendInmobiliarios if rg.idBacken === r.id
    } yield r
  }

  private def getAgentPermission(idAgente: Int) = {
    val k = for {
      a <- usuariosAgentesInmobiliarios if a.id === idAgente
      p <- permisosInmobiliarios if p.idAgente === a.id
      g <- recursosGraficosInmobiliarios if g.id === p.tipoPermiso
    } yield g

    k ++ recursosGraficosInmobiliarios.filter(_.id === 13)
  }

}
