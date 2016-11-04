package portal.transaccional.autenticacion.service.util.JsonFormatters

import co.com.alianza.exceptions.{ NoAutorizado, ValidacionException }
import co.com.alianza.infrastructure.dto.{ Pregunta, Respuesta }
import co.com.alianza.persistence.entities.{ Empresa, RecursoComercial, ReglaContrasena, RolComercial }
import enumerations.{ TipoIdentificacionComercialDTO, TipoIdentificacionDTO }
import portal.transaccional.autenticacion.service.dto.{ PermisoRecursoDTO, RecursoDTO }
import portal.transaccional.autenticacion.service.util.ws.CommonRESTFul
import portal.transaccional.autenticacion.service.web.actualizacion._
import portal.transaccional.autenticacion.service.web.autenticacion.{ AutenticarRequest, AutenticarUsuarioComercialRequest, AutenticarUsuarioEmpresarialRequest }
import portal.transaccional.autenticacion.service.web.autorizacion.InvalidarTokenRequest
import portal.transaccional.autenticacion.service.web.comercial.{ ActualizarContrasenaRequest, CrearAdministradorRequest, ValidarEmpresaRequest }
import portal.transaccional.autenticacion.service.web.horarioEmpresa.{ AgregarHorarioEmpresaRequest, DiaFestivoRequest, ResponseObtenerHorario }
import portal.transaccional.autenticacion.service.web.ip.{ IpRequest, IpResponse }
import portal.transaccional.autenticacion.service.web.pin.ContrasenaUsuario
import portal.transaccional.autenticacion.service.web.preguntasAutovalidacion.{ GuardarRespuestasRequest, ResponseObtenerPreguntas, ResponseObtenerPreguntasComprobar, RespuestasComprobacionRequest }

trait DomainJsonFormatters {

  this: CommonRESTFul =>

  //autenticacion
  implicit val autenticarFormatter = jsonFormat3(AutenticarRequest)
  implicit val autenticarUsuarioEmpresarialFormatter = jsonFormat3(AutenticarUsuarioEmpresarialRequest)
  implicit val autenticarUsuarioComercialRequestFormatter = jsonFormat3(AutenticarUsuarioComercialRequest)

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

}
