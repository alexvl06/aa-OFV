package portal.transaccional.autenticacion.service.util.JsonFormatters

import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.commons.enumerations.TiposCliente.TiposCliente
import co.com.alianza.exceptions.{ ValidacionException, ValidacionExceptionPasswordRules }
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import co.com.alianza.infrastructure.dto.{ Pregunta, Respuesta, UsuarioInmobiliarioAuth }
import co.com.alianza.persistence.entities._
import portal.transaccional.autenticacion.service.dto.{ PermisoRecursoDTO, RecursoDTO }
import portal.transaccional.autenticacion.service.util.ws.{ CommonRESTFul, GenericNoAutorizado }
import portal.transaccional.autenticacion.service.web.agenteInmobiliario._
import portal.transaccional.autenticacion.service.web.autenticacion.{ AutenticarRequest, AutenticarUsuarioComercialRequest, AutenticarUsuarioEmpresarialRequest }
import portal.transaccional.autenticacion.service.web.autorizacion.{ InvalidarTokenRequest, ValidarTokenAgenteRequest }
import portal.transaccional.autenticacion.service.web.ip.AgregarIpRequest
import portal.transaccional.autenticacion.service.web.preguntasAutovalidacion.{ GuardarRespuestasRequest, ResponseObtenerPreguntas, ResponseObtenerPreguntasComprobar, RespuestasComprobacionRequest }
import spray.json.{ DefaultJsonProtocol, DeserializationException, JsNumber, JsObject, JsString, JsValue, JsonFormat, RootJsonFormat }

trait DomainJsonFormatters {

  this: CommonRESTFul =>

  //autenticacion
  implicit val autenticarFormatter = jsonFormat3(AutenticarRequest)
  implicit val autenticarUsuarioEmpresarialFormatter = jsonFormat3(AutenticarUsuarioEmpresarialRequest)
  implicit val autenticarUsuarioComercialRequestFormatter = jsonFormat3(AutenticarUsuarioComercialRequest)

  //autorizacion
  implicit val invalidarTokenFormatter = jsonFormat1(InvalidarTokenRequest)

  //validacion
  implicit val validacionExceptionFormatter = jsonFormat2(ValidacionException)
  implicit val validacionExceptionPasswordRulesFormatter = jsonFormat5(ValidacionExceptionPasswordRules)

  //preguntasAutovalidacion
  implicit val preguntaFormatter = jsonFormat2(Pregunta)
  implicit val responseObtenerPreguntasFormatter = jsonFormat2(ResponseObtenerPreguntas)
  implicit val responseObtenerPreguntasComprobar = jsonFormat2(ResponseObtenerPreguntasComprobar)

  // respuestas
  implicit val respuestaFormatter = jsonFormat2(Respuesta)
  implicit val respuestasRequestFormatter = jsonFormat1(GuardarRespuestasRequest)
  implicit val respuestasComprobacionRequest = jsonFormat2(RespuestasComprobacionRequest)

  //ip
  implicit val ipFormatter = jsonFormat2(AgregarIpRequest)

  //recurso graficos comercial
  implicit val rolesFormater = jsonFormat2(RolComercial)
  implicit val recursosFormater = jsonFormat3(RecursoComercial)
  implicit val recursoDtoFormater = jsonFormat2(RecursoDTO)
  implicit val permisoDtoFormater = jsonFormat1(PermisoRecursoDTO)

  //permisosInmobiliarios
  implicit val enumTipoCLienteFormatter = jsonEnum(TiposCliente)
  implicit val permisoAgenteInmobFormatter: RootJsonFormat[PermisoAgenteInmobiliario] = jsonFormat4(PermisoAgenteInmobiliario)
  implicit val crearAgenteInmobRequestFormatter = jsonFormat5(CrearAgenteInmobiliarioRequest)
  implicit val consultarAgenteInmobResponseFormatter = jsonFormat7(ConsultarAgenteInmobiliarioResponse)
  implicit val paginationMetadataResponseFormatter = jsonFormat5(PaginacionMetadata)
  implicit val consultarAgenteInmobiliarioListResponseFormatter = jsonFormat2(ConsultarAgenteInmobiliarioListResponse)
  implicit val recursosInmobiliariosFormatter = jsonFormat6(RecursoGraficoInmobiliario)
  implicit val actualizarCredencialesRequestFormatter = jsonFormat3(ActualizarCredencialesAgenteRequest)
  implicit val valdiarTokenFormatter = jsonFormat1(ValidarTokenAgenteRequest)
  implicit val messageFormat2 = jsonFormat5(UsuarioInmobiliarioAuth)
  implicit val asdasds = jsonFormat2(GenericNoAutorizado)

  //   ----- MAPEO DE ENUM!
  private def jsonEnum[T <: Enumeration](enu: T) = new JsonFormat[T#Value] {
    def write(obj: T#Value) = JsString(obj.toString)

    def read(json: JsValue) = json match {
      case JsString(txt) => enu.withName(txt)
      case something => throw DeserializationException(s"Expected a value from enum $enu instead of $something")
    }
  }
}
