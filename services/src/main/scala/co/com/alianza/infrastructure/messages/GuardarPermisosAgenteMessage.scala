package co.com.alianza.infrastructure.messages

import spray.json.DefaultJsonProtocol
import spray.httpx.SprayJsonSupport
import spray.json.{JsValue, RootJsonFormat}

import co.com.alianza.infrastructure.dto._
import co.com.alianza.infrastructure.dto.security.UsuarioAuth

/**
 * Created by manuel on 7/01/15.
 */
case class GuardarPermisosAgenteMessage (idAgente: Int, permisos: List[Permiso], encargosPermisos: List[EncargoPermisos], idClienteAdmin: Option[Int]) extends MessageService
case class ConsultarPermisosAgenteMessage (idAgente: Int) extends MessageService
case class ConsultarPermisosAgenteLoginMessage (agente: UsuarioAuth) extends MessageService
case class PermisosRespuesta (permisos: List[Permiso], encargosPermisos: List[EncargoPermisos])
case class PermisosLoginRespuesta (permiteInscripciones:Boolean, permiteTransferencias:Boolean, permitePagosMasivos:Boolean, permiteConsultas:Boolean, permiteProgramacion:Boolean)

object PermisosTransaccionalesJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val AgenteFormat = jsonFormat2(Autorizador)

  implicit val PermisoTransaccionalUsuarioEmpresarialFormat = new RootJsonFormat[PermisoTransaccionalUsuarioEmpresarial]{
    def read(json: JsValue) = {
      val fields = json.asJsObject.fields
      PermisoTransaccionalUsuarioEmpresarial(
        "", 0, fields.get("tipoTransaccion").get.convertTo[Int], fields.get("tipoPermiso").get.convertTo[Int],
        if(fields.get("montoMaximoTransaccion").isDefined) fields.get("montoMaximoTransaccion").get.convertTo[Option[Double]] else Some(0),
        if(fields.get("montoMaximoDiario").isDefined) fields.get("montoMaximoDiario").get.convertTo[Option[Double]] else Some(0),
        if(fields.get("minimoNumeroPersonas").isDefined) fields.get("minimoNumeroPersonas").get.convertTo[Option[Int]] else Some(0),
        if(fields.get("seleccionado").isDefined) fields.get("seleccionado").get.convertTo[Boolean] else false
      )
    }
    def write(p: PermisoTransaccionalUsuarioEmpresarial) = jsonFormat8(PermisoTransaccionalUsuarioEmpresarial).write(p)
  }

  implicit val PermisoAgenteFormat = jsonFormat7(PermisoAgente)
  implicit val PermisoFormat = new RootJsonFormat[Permiso] {
    def read(json: JsValue) = {
      val permiso = jsonFormat2(Permiso).read(json)
      Permiso( permisoAgente = Some(json.asJsObject.convertTo[PermisoAgente]), autorizadores = permiso.autorizadores )
    }
    def write(p: Permiso) = jsonFormat2(Permiso).write(p)
  }

  implicit val PermisoTransaccionalUsuarioEmpresarialAgentesFormat = new RootJsonFormat[PermisoTransaccionalUsuarioEmpresarialAgentes]{
    def read(json: JsValue) = {
      val permisoAgentes = jsonFormat2(PermisoTransaccionalUsuarioEmpresarialAgentes).read(json)
      PermisoTransaccionalUsuarioEmpresarialAgentes(
        Some(json.asJsObject.convertTo[PermisoTransaccionalUsuarioEmpresarial]),
        permisoAgentes.agentes
      )
    }
    def write(p: PermisoTransaccionalUsuarioEmpresarialAgentes) = jsonFormat2(PermisoTransaccionalUsuarioEmpresarialAgentes).write(p)
  }

  implicit val EncargoPermisosFormat = jsonFormat2(EncargoPermisos)
  implicit val GuardarPermisosAgenteFormat = jsonFormat4(GuardarPermisosAgenteMessage)
  implicit val PermisosFormat = jsonFormat2(PermisosRespuesta)

}