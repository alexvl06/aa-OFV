package co.com.alianza.infrastructure.dto

/**
 * Created by hernando on 14/07/15.
 */

case class Pais(pais_pais: Int, pais_descri: String)

case class Cuidad

case class TipoCorreo

case class EnvioCorrespondencia

case class Ocupacion

case class ActividadEconomica

case class DatosCliente(p_fdpn_nombre2: String,
                        p_fdpn_apell1: String,
                        p_fdpn_apell2: String,
                        p_fdpn_pais_residencia: String,
                        p_fdpn_drcl_dire_res: String,
                        p_fdpn_drcl_dire_ofi: String,
                        p_fdpn_drcl_ciud_res: String,
                        p_fdpn_drcl_tele_res: BigInt,
                        p_fdpn_dcfd_email_ant: String,
                        p_fdpn_dcfd_email: String,
                        p_fdpn_dcfd_tipo_ant: String,
                        p_fdpn_dcfd_tipo: String,
                        p_fdpn_envio_corresp: String,
                        p_fdpn_telefono_movil_1: String,
                        p_fdpn_pais_tel_mov_1: String,
                        p_fdpn_ocupacion: String,
                        p_fdpn_if_declara_renta: String,
                        p_fdpn_pafd_pais_ant: String,
                        p_fdpn_pafd_pais: String,
                        p_fdpn_ciua: String/*,
                        p_fdpn_nombre_emp: String,
                        p_fdpn_nit_emp: String,
                        p_fdpn_cargo: String,
                        p_fdpn_dire_emp: String,
                        p_fdpn_ciud_emp: String,
                        p_fdpn_tele_emp: String,
                        p_fdpn_if_vactivos: Double,
                        p_fdpn_if_vpasivos: Double,
                        p_fdpn_if_vpatrimonio: Double,
                        p_fdpn_if_vingresos: Double,
                        p_fdpn_if_vegresos: Double,
                        p_fdpn_if_vingresos_noop_mes: Double,
                        fdpn_fecha_ult_act: DateTime*/
                         )
