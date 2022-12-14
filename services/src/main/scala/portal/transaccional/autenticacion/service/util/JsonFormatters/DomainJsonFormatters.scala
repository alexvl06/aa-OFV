package portal.transaccional.autenticacion.service.util.JsonFormatters

import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.exceptions.{ NoAutorizado, ValidacionException, ValidacionExceptionPasswordRules }
import co.com.alianza.infrastructure.dto.{ UsuarioInmobiliarioAuth, Pregunta, Respuesta }
import co.com.alianza.persistence.entities._
import enumerations.{ TipoIdentificacionComercialDTO, TipoIdentificacionDTO }
import portal.transaccional.autenticacion.service.drivers.menu._
import portal.transaccional.autenticacion.service.drivers.smtp.Mensaje
import portal.transaccional.autenticacion.service.dto.{ PermisoRecursoDTO, RecursoDTO }
import portal.transaccional.autenticacion.service.util.ws.{ GenericNoAutorizado, CommonRESTFul }
import portal.transaccional.autenticacion.service.web.actualizacion._
import portal.transaccional.autenticacion.service.web.agenteInmobiliario._
import portal.transaccional.autenticacion.service.web.autenticacion.{ AutenticarRequest, AutenticarUsuarioComercialRequest, AutenticarUsuarioEmpresarialRequest, UsuarioGenRequest }
import portal.transaccional.autenticacion.service.web.autorizacion.{ InvalidarTokenRequest, ValidarTokenAgenteRequest }
import portal.transaccional.autenticacion.service.web.comercial.{ ActualizarContrasenaRequest, CrearAdministradorRequest, ValidarEmpresaRequest }
import portal.transaccional.autenticacion.service.web.contrasena._
import portal.transaccional.autenticacion.service.web.horarioEmpresa.{ AgregarHorarioEmpresaRequest, DiaFestivoRequest, ResponseObtenerHorario }
import portal.transaccional.autenticacion.service.web.ip.{ IpRequest, IpResponse }
import portal.transaccional.autenticacion.service.web.pin.ContrasenaUsuario
import portal.transaccional.autenticacion.service.web.preguntasAutovalidacion.{ GuardarRespuestasRequest, ResponseObtenerPreguntas, ResponseObtenerPreguntasComprobar, RespuestasComprobacionRequest }
import spray.json._

import scala.collection.mutable.ListBuffer

trait DomainJsonFormatters {

  this: CommonRESTFul =>

  //autenticacion
  implicit val autenticarFormatter = jsonFormat3(AutenticarRequest)
  implicit val autenticarUsuarioEmpresarialFormatter = jsonFormat3(AutenticarUsuarioEmpresarialRequest)
  implicit val autenticarUsuarioComercialRequestFormatter = jsonFormat4(AutenticarUsuarioComercialRequest)
  /**OFV LOGIN FASE 1**/
  implicit val autenticarUsuarioGenRequest = jsonFormat6(UsuarioGenRequest)

  //autorizacion
  implicit val invalidarTokenFormatter = jsonFormat1(InvalidarTokenRequest)

  //actualizacion
  implicit val datosEmpresaMessageFormat = jsonFormat17(DatosEmpresaMessage)
  implicit val actualizacionMessageFormat = jsonFormat20(ActualizacionMessage)
  implicit val paisFormat = jsonFormat2(Pais)
  implicit val ocupacionFormat = jsonFormat2(Ocupacion)
  implicit val tipoCorreoFormat = jsonFormat2(TipoCorreo)
  implicit val ciudadFormat = jsonFormat2(Ciudad)
  implicit val actividadFormat = jsonFormat2(ActividadEconomica)
  implicit val correspondenciaFormat = jsonFormat2(EnvioCorrespondencia)

  //validacion
  implicit val validacionExceptionFormatter = jsonFormat2(ValidacionException)
  implicit val validacionExceptionPasswordRulesFormatter = jsonFormat5(ValidacionExceptionPasswordRules)
  implicit val noAutorizadoExceptionFormatter = jsonFormat1(NoAutorizado)

  //preguntasAutovalidacion
  implicit val preguntaFormatter = jsonFormat2(Pregunta)
  implicit val responseObtenerPreguntasFormatter = jsonFormat2(ResponseObtenerPreguntas)
  implicit val responseObtenerPreguntasComprobar = jsonFormat2(ResponseObtenerPreguntasComprobar)

  // respuestas
  implicit val respuestaFormatter = jsonFormat2(Respuesta)
  implicit val respuestasRequestFormatter = jsonFormat1(GuardarRespuestasRequest)
  implicit val respuestasComprobacionRequest = jsonFormat2(RespuestasComprobacionRequest)

  //ip
  implicit val ipRequestFormatter = jsonFormat1(IpRequest)
  implicit val ipResponseFormatter = jsonFormat1(IpResponse)

  //recurso graficos comercial
  implicit val rolesFormater = jsonFormat2(RolComercial)
  implicit val recursosFormater = jsonFormat3(RecursoComercial)
  implicit val recursoDtoFormater = jsonFormat2(RecursoDTO)
  implicit val permisoDtoFormater = jsonFormat1(PermisoRecursoDTO)

  //comercial
  implicit val crearAdministradorRequestFormater = jsonFormat4(CrearAdministradorRequest)
  implicit val ActualizarContrasenaRequestFormater = jsonFormat2(ActualizarContrasenaRequest)
  implicit val validarEmpresaRequestFormater = jsonFormat1(ValidarEmpresaRequest)
  implicit val EmpresaFormater = jsonFormat3(Empresa)

  //reglas contrasena
  implicit val reglaContrasenaFormatter = jsonFormat2(ReglaContrasena)

  //horarioEmpresa
  implicit val agregarHorarioEmpresaRequestFormatter = jsonFormat4(AgregarHorarioEmpresaRequest)
  implicit val diaFestivoRequestFormatter = jsonFormat1(DiaFestivoRequest)
  implicit val HorarioEmpresaFormatter = jsonFormat4(ResponseObtenerHorario)

  //Enumeracion
  implicit val tipoIdentificacionDTOFormatter = jsonFormat2(TipoIdentificacionDTO)
  implicit val TipoIdentificacionComercialDTOFormatter = jsonFormat4(TipoIdentificacionComercialDTO)

  //pin
  implicit val cambioContrasenaFormatter = jsonFormat2(ContrasenaUsuario)

  //permisosInmobiliarios
  implicit val enumTipoCLienteFormatter = jsonEnum(TiposCliente)
  implicit val permisoAgenteInmobFormatter: RootJsonFormat[PermisoAgenteInmobiliario] = jsonFormat4(PermisoAgenteInmobiliario)
  implicit val crearAgenteInmobRequestFormatter = jsonFormat6(CrearAgenteInmobiliarioRequest)
  implicit val consultarAgenteInmobResponseFormatter = jsonFormat8(ConsultarAgenteInmobiliarioResponse)
  implicit val paginationMetadataResponseFormatter = jsonFormat5(PaginacionMetadata)
  implicit val consultarAgenteInmobiliarioListResponseFormatter = jsonFormat2(ConsultarAgenteInmobiliarioListResponse)
  implicit val recursosInmobiliariosFormatter = jsonFormat7(RecursoGraficoInmobiliario)
  implicit val actualizarCredencialesRequestFormatter = jsonFormat3(ActualizarCredencialesAgenteRequest)
  implicit val valdiarTokenFormatter = jsonFormat1(ValidarTokenAgenteRequest)
  implicit val messageFormat2 = jsonFormat5(UsuarioInmobiliarioAuth)
  implicit val asdasds = jsonFormat2(GenericNoAutorizado)

  //smtp
  implicit val mensajeFormatter = jsonFormat5(Mensaje)

  //contrasenas
  implicit val reiniciarContrasenaAgenteFormatter = jsonFormat1(ReiniciarContrasenaAgente)
  implicit val cambiarEstadoAgenteFormatter = jsonFormat1(CambiarEstadoAgente)
  implicit val cambiarContrasenaFormatter = jsonFormat2(CambiarContrasena)
  implicit val cambiarContrasenaCaducadaFormatter = jsonFormat3(CambiarContrasenaCaducada)

  /**OFV LOGIN FASE 1**/
  implicit def listBufferFormat[T: JsonFormat] = new RootJsonFormat[ListBuffer[T]] {
    def write(listBuffer: ListBuffer[T]) = JsArray(listBuffer.map(_.toJson).toVector)
    def read(value: JsValue): ListBuffer[T] = value match {
      case JsArray(elements) => elements.map(_.convertTo[T])(collection.breakOut)
      case x => deserializationError("Expected ListBuffer as JsArray, but got " + x)
    }
  }
  /**OFV LOGIN FASE 1**/
  //MenuResponse
  implicit val menuSubItemUsuario = jsonFormat4(SubItemMenu)
  implicit val menuItemSubUsuario = jsonFormat5(ItemMenuSub)
  implicit val menuItemUsuario = jsonFormat5(ItemMenu)
  implicit val menuUsuarioGen = jsonFormat2(MenuUsuario)
  implicit val menuResponse = jsonFormat1(MenuResponse)

  //   ----- MAPEO DE ENUM!
  private def jsonEnum[T <: Enumeration](enu: T) = new JsonFormat[T#Value] {
    def write(obj: T#Value) = JsString(obj.toString)

    def read(json: JsValue) = json match {
      case JsString(txt) => enu.withName(txt)
      case something => throw DeserializationException(s"Expected a value from enum $enu instead of $something")
    }
  }
}
