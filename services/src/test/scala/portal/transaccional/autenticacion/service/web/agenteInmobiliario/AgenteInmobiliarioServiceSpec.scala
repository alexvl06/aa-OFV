package portal.transaccional.autenticacion.service.web.agenteInmobiliario

import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import portal.transaccional.autenticacion.service.drivers.contrasenaAgenteInmobiliario.ContrasenaAgenteInmobiliarioRepository
import portal.transaccional.autenticacion.service.drivers.permisoAgenteInmobiliario.PermisoAgenteInmobiliarioRepository
import portal.transaccional.autenticacion.service.drivers.usuarioAgenteInmobiliario.UsuarioInmobiliarioRepository
import portal.transaccional.autenticacion.service.web.RouteTest
import spray.http.StatusCodes

import scala.concurrent.Future

// scalastyle:off
class AgenteInmobiliarioServiceSpec extends RouteTest {

  // ---------- GET /agentes-inmobiliarios ------------------

  def getAgentesInmobiliariosStub(constructor: UsuarioAuth): AgenteInmobiliarioService = {

    val agentesRepo = stub[UsuarioInmobiliarioRepository]
    val permisosRepo = stub[PermisoAgenteInmobiliarioRepository]
    val contrasenasRepo = stub[ContrasenaAgenteInmobiliarioRepository]

    // empty list
    (agentesRepo.getAgenteInmobiliarioList _).when("", None, None, None, None, None, None)
      .returns(Future.successful(ConsultarAgenteInmobiliarioListResponse(PaginacionMetadata(0, 0, 0, 0, None), Seq.empty)))

    // list with one element
    (agentesRepo.getAgenteInmobiliarioList _).when("1", None, None, None, None, None, None)
      .returns(Future.successful(ConsultarAgenteInmobiliarioListResponse(
        PaginacionMetadata(1, 1, 1, 1, None),
        Seq(ConsultarAgenteInmobiliarioResponse(1, "agente@constructor.com", "agente", 1, Some("Agente"), None, None))
      )))

    // paginated list
    (agentesRepo.getAgenteInmobiliarioList _).when("2", None, None, None, None, Some(1), Some(1))
      .returns(Future.successful(ConsultarAgenteInmobiliarioListResponse(
        PaginacionMetadata(1, 1, 1, 3, None),
        Seq(ConsultarAgenteInmobiliarioResponse(1, "agente@constructor.com", "agente", 1, Some("Agente"), None, None))
      )))

    (agentesRepo.getAgenteInmobiliarioList _).when("2", None, None, None, None, Some(2), Some(1))
      .returns(Future.successful(ConsultarAgenteInmobiliarioListResponse(
        PaginacionMetadata(2, 1, 1, 3, None),
        Seq(ConsultarAgenteInmobiliarioResponse(1, "agente2@constructor.com", "agente2", 1, Some("Agente2"), None, None))
      )))

    (agentesRepo.getAgenteInmobiliarioList _).when("2", None, None, None, None, Some(3), Some(1))
      .returns(Future.successful(ConsultarAgenteInmobiliarioListResponse(
        PaginacionMetadata(3, 1, 1, 3, None),
        Seq(ConsultarAgenteInmobiliarioResponse(1, "agente3@constructor.com", "agente3", 1, Some("Agente3"), None, None))
      )))

    // error
    (agentesRepo.getAgenteInmobiliarioList _).when("error", None, None, None, None, None, None)
      .returns(Future.failed(new Exception()))

    AgenteInmobiliarioService(constructor, agentesRepo, permisosRepo, contrasenasRepo)
  }

  "AgenteInmobiliarioService" should "GET /agentes-inmobiliarios - respond an empty list - invalid constructor" in {
    val constructor = UsuarioAuth(1, TiposCliente.clienteAdminInmobiliario, "", 3)
    Get("/agentes-inmobiliarios") ~> getAgentesInmobiliariosStub(constructor).route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[ConsultarAgenteInmobiliarioListResponse].agentes shouldEqual Seq.empty
    }
  }

  it should "GET /agentes-inmobiliarios - respond a list with one element - constructor with id 1" in {
    val constructor = UsuarioAuth(1, TiposCliente.clienteAdminInmobiliario, "1", 3)
    Get("/agentes-inmobiliarios") ~> getAgentesInmobiliariosStub(constructor).route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[ConsultarAgenteInmobiliarioListResponse].agentes shouldEqual
        Seq(ConsultarAgenteInmobiliarioResponse(1, "agente@constructor.com", "agente", 1, Some("Agente"), None, None))
    }
  }

  it should "GET /agentes-inmobiliarios - respond a paginated list - constructor with id 2" in {
    val constructor = UsuarioAuth(1, TiposCliente.clienteAdminInmobiliario, "2", 3)

    Get("/agentes-inmobiliarios?pagina=1&itemsPorPagina=1") ~> getAgentesInmobiliariosStub(constructor).route ~> check {
      status shouldEqual StatusCodes.OK
      val resp = responseAs[ConsultarAgenteInmobiliarioListResponse]
      resp._metadata.links.isDefined shouldBe true
      resp._metadata.links.get.get("self") shouldBe Some("/agentes-inmobiliarios?pagina=1&itemsPorPagina=1")
      resp._metadata.links.get.get("first") shouldBe Some("/agentes-inmobiliarios?pagina=1&itemsPorPagina=1")
      resp._metadata.links.get.get("next") shouldBe Some("/agentes-inmobiliarios?pagina=2&itemsPorPagina=1")
      resp._metadata.links.get.get("last") shouldBe Some("/agentes-inmobiliarios?pagina=3&itemsPorPagina=1")
      resp._metadata.links.get.get("previous") shouldBe None
      responseAs[ConsultarAgenteInmobiliarioListResponse].agentes shouldEqual
        Seq(ConsultarAgenteInmobiliarioResponse(1, "agente@constructor.com", "agente", 1, Some("Agente"), None, None))
    }

    Get("/agentes-inmobiliarios?pagina=2&itemsPorPagina=1") ~> getAgentesInmobiliariosStub(constructor).route ~> check {
      status shouldEqual StatusCodes.OK
      val resp = responseAs[ConsultarAgenteInmobiliarioListResponse]
      resp._metadata.links.isDefined shouldBe true
      resp._metadata.links.get.get("self") shouldBe Some("/agentes-inmobiliarios?pagina=2&itemsPorPagina=1")
      resp._metadata.links.get.get("first") shouldBe Some("/agentes-inmobiliarios?pagina=1&itemsPorPagina=1")
      resp._metadata.links.get.get("next") shouldBe Some("/agentes-inmobiliarios?pagina=3&itemsPorPagina=1")
      resp._metadata.links.get.get("last") shouldBe Some("/agentes-inmobiliarios?pagina=3&itemsPorPagina=1")
      resp._metadata.links.get.get("previous") shouldBe Some("/agentes-inmobiliarios?pagina=1&itemsPorPagina=1")
      responseAs[ConsultarAgenteInmobiliarioListResponse].agentes shouldEqual
        Seq(ConsultarAgenteInmobiliarioResponse(1, "agente2@constructor.com", "agente2", 1, Some("Agente2"), None, None))
    }

    Get("/agentes-inmobiliarios?pagina=3&itemsPorPagina=1") ~> getAgentesInmobiliariosStub(constructor).route ~> check {
      status shouldEqual StatusCodes.OK
      val resp = responseAs[ConsultarAgenteInmobiliarioListResponse]
      resp._metadata.links.isDefined shouldBe true
      resp._metadata.links.get.get("self") shouldBe Some("/agentes-inmobiliarios?pagina=3&itemsPorPagina=1")
      resp._metadata.links.get.get("first") shouldBe Some("/agentes-inmobiliarios?pagina=1&itemsPorPagina=1")
      resp._metadata.links.get.get("next") shouldBe None
      resp._metadata.links.get.get("last") shouldBe Some("/agentes-inmobiliarios?pagina=3&itemsPorPagina=1")
      resp._metadata.links.get.get("previous") shouldBe Some("/agentes-inmobiliarios?pagina=2&itemsPorPagina=1")
      responseAs[ConsultarAgenteInmobiliarioListResponse].agentes shouldEqual
        Seq(ConsultarAgenteInmobiliarioResponse(1, "agente3@constructor.com", "agente3", 1, Some("Agente3"), None, None))
    }
  }

  it should "GET /agentes-inmobiliarios - respond status code 500-InternalServerError - an exception occurs" in {
    val constructor = UsuarioAuth(1, TiposCliente.clienteAdminInmobiliario, "error", 3)
    Get("/agentes-inmobiliarios") ~> getAgentesInmobiliariosStub(constructor).route ~> check {
      status shouldEqual StatusCodes.InternalServerError
    }
  }

  // ---------- POST /agentes-inmobiliarios ------------------

  def postAgentesInmobiliariosStub(constructor: UsuarioAuth): AgenteInmobiliarioService = {
    val agentesRepo = stub[UsuarioInmobiliarioRepository]
    val permisosRepo = stub[PermisoAgenteInmobiliarioRepository]
    val contrasenasRepo = stub[ContrasenaAgenteInmobiliarioRepository]

    (agentesRepo.createAgenteInmobiliario _).when(1, 3, "1", "agente@constructor.com", "agente", Some("Agente"), None, None)
      .returns(Future.successful(1))

    (agentesRepo.createAgenteInmobiliario _).when(1, 3, "1", "agente@constructor.com", "agenteYaExiste", Some("Agente"), None, None)
      .returns(Future.successful(0))

    (agentesRepo.createAgenteInmobiliario _).when(1, 3, "error", "agente@constructor.com", "agente", Some("Agente"), None, None)
      .returns(Future.failed(new Exception()))

    AgenteInmobiliarioService(constructor, agentesRepo, permisosRepo, contrasenasRepo)
  }

  it should "POST /agentes-inmobiliarios - agent created successfully" in {
    val constructor = UsuarioAuth(1, TiposCliente.clienteAdminInmobiliario, "1", 3)
    val agenteACrear = CrearAgenteInmobiliarioRequest("agente@constructor.com", "agente", Some("Agente"), None, None)
    Post("/agentes-inmobiliarios", agenteACrear) ~> postAgentesInmobiliariosStub(constructor).route ~> check {
      status shouldEqual StatusCodes.Created
    }
  }

  it should "POST /agentes-inmobiliarios - respond status code 409-Conflict - agent already exists" in {
    val constructor = UsuarioAuth(1, TiposCliente.clienteAdminInmobiliario, "1", 3)
    val agenteACrear = CrearAgenteInmobiliarioRequest("agente@constructor.com", "agenteYaExiste", Some("Agente"), None, None)
    Post("/agentes-inmobiliarios", agenteACrear) ~> postAgentesInmobiliariosStub(constructor).route ~> check {
      println(status)
      status shouldEqual StatusCodes.Conflict
    }
  }

  it should "POST /agentes-inmobiliarios - respond status code 500-InternalServerError - an exception occurs" in {
    val constructor = UsuarioAuth(1, TiposCliente.clienteAdminInmobiliario, "error", 3)
    val agenteACrear = CrearAgenteInmobiliarioRequest("agente@constructor.com", "agente", Some("Agente"), None, None)
    Post("/agentes-inmobiliarios", agenteACrear) ~> postAgentesInmobiliariosStub(constructor).route ~> check {
      status shouldEqual StatusCodes.InternalServerError
    }
  }

  // ---------- GET /agentes-inmobiliarios/{usuario-agente} ------------------

  def getAgentesInmobiliariosDetailStub(constructor: UsuarioAuth): AgenteInmobiliarioService = {
    val agentesRepo = stub[UsuarioInmobiliarioRepository]
    val permisosRepo = stub[PermisoAgenteInmobiliarioRepository]
    val contrasenasRepo = stub[ContrasenaAgenteInmobiliarioRepository]

    (agentesRepo.getAgenteInmobiliario(_: String, _: String)).when("1", "agente")
      .returns(Future.successful(Some(ConsultarAgenteInmobiliarioResponse(1, "agente@constructor.com", "agente", 1, Some("Agente"), None, None))))

    (agentesRepo.getAgenteInmobiliario(_: String, _: String)).when("1", "agenteInvalido")
      .returns(Future.successful(None))

    (agentesRepo.getAgenteInmobiliario(_: String, _: String)).when("1", "error")
      .returns(Future.failed(new Exception()))

    AgenteInmobiliarioService(constructor, agentesRepo, permisosRepo, contrasenasRepo)
  }

  it should "GET /agentes-inmobiliarios/{usuario-agente} - respond agent details - constructor with id '1', agent with username 'agente'" in {
    val constructor = UsuarioAuth(1, TiposCliente.clienteAdminInmobiliario, "1", 3)
    Get("/agentes-inmobiliarios/agente") ~> getAgentesInmobiliariosDetailStub(constructor).route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[ConsultarAgenteInmobiliarioResponse] shouldEqual
        ConsultarAgenteInmobiliarioResponse(1, "agente@constructor.com", "agente", 1, Some("Agente"), None, None)
    }
  }

  it should "GET /agentes-inmobiliarios/{usuario-agente} - respond status code 404-NotFound - agent does not exist" in {
    val constructor = UsuarioAuth(1, TiposCliente.clienteAdminInmobiliario, "1", 3)
    Get("/agentes-inmobiliarios/agenteInvalido") ~> getAgentesInmobiliariosDetailStub(constructor).route ~> check {
      status shouldEqual StatusCodes.NotFound
    }
  }

  it should "GET /agentes-inmobiliarios/{usuario-agente} - respond status code 500-InternalServerError - an exception occurs" in {
    val constructor = UsuarioAuth(1, TiposCliente.clienteAdminInmobiliario, "1", 3)
    Get("/agentes-inmobiliarios/error") ~> getAgentesInmobiliariosDetailStub(constructor).route ~> check {
      status shouldEqual StatusCodes.InternalServerError
    }
  }

  // ---------- PUT /agentes-inmobiliarios/{usuario-agente} ------------------

  def putAgentesInmobiliariosDetailStub(constructor: UsuarioAuth): AgenteInmobiliarioService = {
    val agentesRepo = stub[UsuarioInmobiliarioRepository]
    val permisosRepo = stub[PermisoAgenteInmobiliarioRepository]
    val contrasenasRepo = stub[ContrasenaAgenteInmobiliarioRepository]

    (agentesRepo.updateAgenteInmobiliario _).when("1", "agente", "agenteEditado@constructor.com", Some("Agente"), Some("nuevoCargo"), Some("nuevaDescripcion"))
      .returns(Future.successful(1))

    (agentesRepo.updateAgenteInmobiliario _).when("1", "agenteInvalido", "agenteEditado@constructor.com", Some("Agente"), Some("nuevoCargo"), Some("nuevaDescripcion"))
      .returns(Future.successful(0))

    (agentesRepo.updateAgenteInmobiliario _).when("1", "error", "agenteEditado@constructor.com", Some("Agente"), Some("nuevoCargo"), Some("nuevaDescripcion"))
      .returns(Future.failed(new Exception()))

    AgenteInmobiliarioService(constructor, agentesRepo, permisosRepo, contrasenasRepo)
  }

  it should "PUT /agentes-inmobiliarios/{usuario-agente} - update agent successfully - constructor with id '1', agent with username 'agente'" in {
    val constructor = UsuarioAuth(1, TiposCliente.clienteAdminInmobiliario, "1", 3)
    val agenteEditado = CrearAgenteInmobiliarioRequest("agenteEditado@constructor.com", "agente", Some("Agente"), Some("nuevoCargo"), Some("nuevaDescripcion"))
    Put("/agentes-inmobiliarios/agente", agenteEditado) ~> putAgentesInmobiliariosDetailStub(constructor).route ~> check {
      status shouldEqual StatusCodes.OK
    }
  }

  it should "PUT /agentes-inmobiliarios/{usuario-agente} - respond status code 404-NotFound - agent does not exist" in {
    val constructor = UsuarioAuth(1, TiposCliente.clienteAdminInmobiliario, "1", 3)
    val agenteEditado = CrearAgenteInmobiliarioRequest("agenteEditado@constructor.com", "agente", Some("Agente"), Some("nuevoCargo"), Some("nuevaDescripcion"))
    Put("/agentes-inmobiliarios/agenteInvalido", agenteEditado) ~> putAgentesInmobiliariosDetailStub(constructor).route ~> check {
      status shouldEqual StatusCodes.NotFound
    }
  }

  it should "PUT /agentes-inmobiliarios/{usuario-agente} - respond status code 500-InternalServerError - an exception occurs" in {
    val constructor = UsuarioAuth(1, TiposCliente.clienteAdminInmobiliario, "1", 3)
    val agenteEditado = CrearAgenteInmobiliarioRequest("agenteEditado@constructor.com", "agente", Some("Agente"), Some("nuevoCargo"), Some("nuevaDescripcion"))
    Put("/agentes-inmobiliarios/error", agenteEditado) ~> putAgentesInmobiliariosDetailStub(constructor).route ~> check {
      status shouldEqual StatusCodes.InternalServerError
    }
  }
}
