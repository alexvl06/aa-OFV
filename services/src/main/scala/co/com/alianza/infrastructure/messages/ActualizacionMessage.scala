package co.com.alianza.infrastructure.messages

import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol

/**
 * Created by hernando on 14/07/15.
 */

object ActualizacionMessagesJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val datosEmpresaMessageFormat = jsonFormat17(DatosEmpresaMessage)
  implicit val actualizacionMessageFormat = jsonFormat22(ActualizacionMessage)
}

case class ObtenerPaises() extends MessageService

case class ObtenerTiposCorreo() extends MessageService

case class ObtenerOcupaciones() extends MessageService

case class ObtenerCiudades(pais: Int) extends MessageService

case class ObtenerEnvioCorrespondencia() extends MessageService

case class ObtenerDatos(user: UsuarioAuth) extends MessageService

case class ComprobarDatos(user: UsuarioAuth) extends MessageService

case class ObtenerActividadesEconomicas() extends MessageService

case class ActualizacionMessage(
  fdpn_nombre1: String,
  fdpn_nombre2: String,
  fdpn_apell1: String,
  fdpn_apell2: String,
  fdpn_pais_residencia: String,
  fdpn_drcl_dire_res: String,
  fdpn_drcl_dire_ofi: String,
  fdpn_drcl_ciud_res: String,
  fdpn_drcl_tele_res: String,
  fdpn_dcfd_email: String,
  fdpn_dcfd_email_ant: String,
  fdpn_dcfd_tipo: String,
  fdpn_dcfd_tipo_ant: String,
  fdpn_envio_corresp: String,
  fdpn_telefono_movil_1: String,
  fdpn_pais_tel_mov_1: String,
  fdpn_ocupacion: String,
  fdpn_ciua: String,
  fdpn_fecha_ult_act: String,
  datosEmp: DatosEmpresaMessage,
  idUsuario: Option[Int],
  tipoCliente: Option[String]
) extends MessageService

case class DatosEmpresaMessage(
  fdpn_if_declara_renta: String,
  fdpn_pafd_pais: String,
  fdpn_pafd_pais_ant: String,
  fdpn_nombre_emp: String,
  fdpn_nit_emp: String,
  fdpn_cargo: String,
  fdpn_dire_emp: String,
  fdpn_ciud_emp: String,
  fdpn_ciud_nombre_emp: String,
  fdpn_tele_emp: String,
  fdpn_if_vactivos: String,
  fdpn_if_vpasivos: String,
  fdpn_if_vpatrimonio: String,
  fdpn_if_vingresos: String,
  fdpn_if_vegresos: String,
  fdpn_if_vingresos_noop_mes: String,
  fdpn_usua_comp_datos: String
)
