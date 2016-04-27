package co.com.alianza.infrastructure.dto

/**
 * Created by hernando on 14/07/15.
 */

case class Ocupacion(codigo: String, descri: String)

case class TipoCorreo(codigo: String, descri: String)

case class Pais(pais_pais: String, pais_descri: String)

case class Ciudad(ciud_ciud: String, ciud_descri: String)

case class ActividadEconomica(codigo: String, descri: String)

case class EnvioCorrespondencia(codigo: String, descri: String)

case class DatosCliente(
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
  fdpn_dcfd_tipo: String,
  fdpn_envio_corresp: String,
  fdpn_telefono_movil_1: String,
  fdpn_pais_tel_mov_1: String,
  fdpn_ocupacion: String,
  fdpn_ciua: String,
  fdpn_fecha_ult_act: String,
  `nvl(fdpn_fecha_ult_act,fdpn_fecha_creacion)`: String,
  datosEmp: DatosEmpresa
)

case class DatosEmpresa(
  fdpn_pafd_pais: String,
  fdpn_nombre_emp: String,
  fdpn_nit_emp: String,
  fdpn_cargo: String,
  fdpn_dire_emp: String,
  fdpn_ciud_emp: String,
  fdpn_if_declara_renta: String,
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