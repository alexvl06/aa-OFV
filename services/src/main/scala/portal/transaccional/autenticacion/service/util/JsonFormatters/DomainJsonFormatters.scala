package portal.transaccional.autenticacion.service.util.JsonFormatters

import co.com.alianza.commons.enumerations.TipoPermisoInmobiliario
import co.com.alianza.exceptions.ValidacionException
import co.com.alianza.infrastructure.dto.{ Pregunta, Respuesta }
import co.com.alianza.persistence.entities.{ PermisoAgenteInmobiliario, RecursoAgenteInmobiliario, RecursoComercial, RolComercial }
import portal.transaccional.autenticacion.service.dto.{ PermisoRecursoDTO, RecursoDTO }
import portal.transaccional.autenticacion.service.util.ws.CommonRESTFul
import portal.transaccional.autenticacion.service.web.autenticacion.{ AutenticarRequest, AutenticarUsuarioComercialRequest, AutenticarUsuarioEmpresarialRequest }
import portal.transaccional.autenticacion.service.web.autorizacion.InvalidarTokenRequest
import portal.transaccional.autenticacion.service.web.ip.AgregarIpRequest
import portal.transaccional.autenticacion.service.web.agenteInmobiliario._
import portal.transaccional.autenticacion.service.web.preguntasAutovalidacion.{ GuardarRespuestasRequest, ResponseObtenerPreguntas, ResponseObtenerPreguntasComprobar, RespuestasComprobacionRequest }
import spray.json.{ DeserializationException, JsString, JsValue, JsonFormat }

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
  implicit val tipoPermisosInmobFormatter = jsonEnum(TipoPermisoInmobiliario)
  implicit val edicionPermisosFormatter = jsonFormat4(EdicionPermisoRequest)
  implicit val permisoAgenteInmobFormatter = jsonFormat4(PermisoAgenteInmobiliario)
  implicit val permisosFidRequestFormatter = jsonFormat3(EdicionFidPermisoRequest)
  implicit val crearAgenteInmobRequestFormatter = jsonFormat5(CrearAgenteInmobiliarioRequest)
  implicit val consultarAgenteInmobResponseFormatter = jsonFormat7(ConsultarAgenteInmobiliarioResponse)
  implicit val paginationMetadataResponseFormatter = jsonFormat5(PaginacionMetadata)
  implicit val consultarAgenteInmobiliarioListResponseFormatter = jsonFormat2(ConsultarAgenteInmobiliarioListResponse)
  implicit val recursosInmobiliariosFormatter = jsonFormat3(RecursoAgenteInmobiliario)
  implicit val edicionContrasenaFormatter = jsonFormat2(EdicionContrasena)

  // ----- MAPEO DE ENUM!
  private def jsonEnum[T <: Enumeration](enu: T) = new JsonFormat[T#Value] {
    def write(obj: T#Value) = JsString(obj.toString)

    def read(json: JsValue) = json match {
      case JsString(txt) => enu.withName(txt)
      case something => throw new DeserializationException(s"Expected a value from enum $enu instead of $something")
    }
  }
}
