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

  def getAgentesInmobiliariosStub(constructor: UsuarioAuth): AgenteInmobiliarioService = {
    // --------- dependencies ---------
    val agentesRepo = stub[UsuarioInmobiliarioRepository]
    val permisosRepo = stub[PermisoAgenteInmobiliarioRepository]
    val contrasenasRepo = stub[ContrasenaAgenteInmobiliarioRepository]

    // ----------- stubs -----------

    // empty list
    (agentesRepo.getAgenteInmobiliarioList _)
      .when("", None, None, None, None, None, None)
      .returns(Future.successful(ConsultarAgenteInmobiliarioListResponse(PaginacionMetadata(0, 0, 0, 0, None), Seq.empty)))

    // list with one element
    (agentesRepo.getAgenteInmobiliarioList _)
      .when("1", None, None, None, None, None, None)
      .returns(Future.successful(ConsultarAgenteInmobiliarioListResponse(
        PaginacionMetadata(1, 1, 1, 1, None),
        Seq(ConsultarAgenteInmobiliarioResponse(1, "agente@constructor.com", "agente", 1, Some("Agente"), None, None))
      )))

    // paginated list
    (agentesRepo.getAgenteInmobiliarioList _)
      .when("2", None, None, None, None, Some(1), Some(1))
      .returns(Future.successful(ConsultarAgenteInmobiliarioListResponse(
        PaginacionMetadata(1, 1, 1, 3, None),
        Seq(ConsultarAgenteInmobiliarioResponse(1, "agente@constructor.com", "agente", 1, Some("Agente"), None, None))
      )))

    (agentesRepo.getAgenteInmobiliarioList _)
      .when("2", None, None, None, None, Some(2), Some(1))
      .returns(Future.successful(ConsultarAgenteInmobiliarioListResponse(
        PaginacionMetadata(2, 1, 1, 3, None),
        Seq(ConsultarAgenteInmobiliarioResponse(1, "agente2@constructor.com", "agente2", 1, Some("Agente2"), None, None))
      )))

    (agentesRepo.getAgenteInmobiliarioList _)
      .when("2", None, None, None, None, Some(3), Some(1))
      .returns(Future.successful(ConsultarAgenteInmobiliarioListResponse(
        PaginacionMetadata(3, 1, 1, 3, None),
        Seq(ConsultarAgenteInmobiliarioResponse(1, "agente3@constructor.com", "agente3", 1, Some("Agente3"), None, None))
      )))

    // error
    (agentesRepo.getAgenteInmobiliarioList _)
      .when("error", None, None, None, None, None, None)
      .returns(Future.failed(new RuntimeException()))

    // ------------- build service ----------
    AgenteInmobiliarioService(constructor, agentesRepo, permisosRepo, contrasenasRepo)
  }

  "AgenteInmobiliarioService" should "respond an empty list when trying to GET /agentes-inmobiliarios from an invalid constructor" in {
    val constructor = UsuarioAuth(1, TiposCliente.clienteAdminInmobiliario, "", 3)
    Get("/agentes-inmobiliarios") ~> getAgentesInmobiliariosStub(constructor).route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[ConsultarAgenteInmobiliarioListResponse].agentes shouldEqual Seq.empty
    }
  }

  it should "respond a list with one element when trying to GET /agentes-inmobiliarios from constructor with id 1" in {
    val constructor = UsuarioAuth(1, TiposCliente.clienteAdminInmobiliario, "1", 3)
    Get("/agentes-inmobiliarios") ~> getAgentesInmobiliariosStub(constructor).route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[ConsultarAgenteInmobiliarioListResponse].agentes shouldEqual
        Seq(ConsultarAgenteInmobiliarioResponse(1, "agente@constructor.com", "agente", 1, Some("Agente"), None, None))
    }
  }

  it should "respond a paginated list when trying to GET /agentes-inmobiliarios from constructor with id 2" in {
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

  it should "respond 500 InternalServerError when" in {
    val constructor = UsuarioAuth(1, TiposCliente.clienteAdminInmobiliario, "error", 3)
    Get("/agentes-inmobiliarios?pagina=3&itemsPorPagina=1") ~> getAgentesInmobiliariosStub(constructor).route ~> check {
      status shouldEqual StatusCodes.InternalServerError
    }
  }
}
