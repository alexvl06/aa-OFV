package co.com.alianza.infrastructure.messages

import spray.json.DefaultJsonProtocol
import spray.httpx.SprayJsonSupport
import spray.json.{JsValue, RootJsonFormat}

import co.com.alianza.infrastructure.dto._

/**
 * Created by manuel on 7/01/15.
 */
case class GuardarPermisosAgenteMessage (idAgente: Int, encargosPermisos: List[EncargoPermisos], idClienteAdmin: Option[Int]) extends MessageService
case class ConsultarPermisosAgenteMessage(idAgente: Int) extends MessageService

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
  implicit val PermisoTransaccionalUsuarioEmpresarialAgentesFormat = new RootJsonFormat[PermisoTransaccionalUsuarioEmpresarialAgentes]{
    def read(json: JsValue) = {
      val fields = json.asJsObject.fields
      val permisoAgentes = jsonFormat2(PermisoTransaccionalUsuarioEmpresarialAgentes).read(json)
      PermisoTransaccionalUsuarioEmpresarialAgentes(
        Some(json.asJsObject.convertTo[PermisoTransaccionalUsuarioEmpresarial]),
        permisoAgentes.agentes
      )
    }
    def write(p: PermisoTransaccionalUsuarioEmpresarialAgentes) = jsonFormat2(PermisoTransaccionalUsuarioEmpresarialAgentes).write(p)
  }

  implicit val EncargoPermisosFormat = jsonFormat2(EncargoPermisos)

  implicit val GuardarPermisosAgenteFormat = jsonFormat3(GuardarPermisosAgenteMessage)
}