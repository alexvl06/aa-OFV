package co.com.alianza.infrastructure.messages

import spray.json.DefaultJsonProtocol
import spray.httpx.SprayJsonSupport
import spray.json.{JsValue, RootJsonFormat}

import co.com.alianza.infrastructure.dto.PermisoTransaccionalUsuarioEmpresarial

/**
 * Created by manuel on 7/01/15.
 */
case class GuardarPermisosAgenteMessage (idAgente: Int, encargosPermisos: List[EncargoPermisos]) extends MessageService
case class EncargoPermisos (wspf_plan: String, permisos: List[PermisoTransaccionalUsuarioEmpresarial]) extends MessageService

object PermisosTransaccionalesJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val PermisoTransaccionalUsuarioEmpresarialReader = new RootJsonFormat[PermisoTransaccionalUsuarioEmpresarial]{
    def read(json: JsValue) = {
      val fields = json.asJsObject.fields
      PermisoTransaccionalUsuarioEmpresarial(
        "", 0, fields.get("tipoTransaccion").get.convertTo[Int], fields.get("tipoPermiso").get.convertTo[Int],
        if(fields.get("montoMaximoTransaccion").isDefined) fields.get("montoMaximoTransaccion").get.convertTo[Option[Double]] else Some(0),
        if(fields.get("montoMaximoDiario").isDefined) fields.get("montoMaximoDiario").get.convertTo[Option[Double]] else Some(0),
        if(fields.get("minimoNumeroPersonas").isDefined) fields.get("minimoNumeroPersonas").get.convertTo[Option[Int]] else Some(0) )
    }
    def write(p: PermisoTransaccionalUsuarioEmpresarial) = jsonFormat7(PermisoTransaccionalUsuarioEmpresarial).write(p)
  }
  implicit val EncargoPermisosFormat = jsonFormat2(EncargoPermisos)
  implicit val GuardarPermisosAgenteFormat = jsonFormat2(GuardarPermisosAgenteMessage)
}