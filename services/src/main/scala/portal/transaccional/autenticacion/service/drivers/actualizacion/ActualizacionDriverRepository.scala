package portal.transaccional.autenticacion.service.drivers.actualizacion

import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.exceptions.ValidacionException
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import enumerations.TiposIdentificacionCore
import org.joda.time.DateTime
import portal.transaccional.autenticacion.service.web.actualizacion._
import portal.transaccional.fiduciaria.autenticacion.storage.daos.core.ActualizacionDAO

import scala.concurrent.{ Future, ExecutionContext }

/**
 * Created by hernando on 10/10/16.
 */
case class ActualizacionDriverRepository(actualizacionDAO: ActualizacionDAO)(implicit val ex: ExecutionContext) extends ActualizacionRepository {

  def obtenerPaises(): Future[Seq[Pais]] = {
    transformarString[Seq[Pais]](actualizacionDAO.listarPaises, DataAccessTranslator.translatePaises)
  }

  def obtenerTiposCorreo(): Future[Seq[TipoCorreo]] = {
    transformarString[Seq[TipoCorreo]](actualizacionDAO.listarTipoCorreo, DataAccessTranslator.translateTiposCorreo)
  }

  def obtenerOcupaciones(): Future[Seq[Ocupacion]] = {
    transformarString[Seq[Ocupacion]](actualizacionDAO.listarOcupaciones, DataAccessTranslator.translateOcupaciones)
  }

  def obtenerDatos(user: UsuarioAuth): Future[DatosCliente] = {
    user.tipoCliente match {
      case TiposCliente.clienteIndividual | TiposCliente.clienteAdministrador =>
        val tipoIdCore: String = TiposIdentificacionCore.getTipoIdentificacion(user.tipoIdentificacion)
        val futuroString: Future[String] = actualizacionDAO.consultaDatosCliente(user.identificacion, tipoIdCore)
        val datosCliente: Future[DatosCliente] = transformarString[DatosCliente](futuroString, DataAccessTranslator.translateDatosCliente)
        datosCliente.map {
          datos =>
            val fechaActualizacion: Option[String] = Option(datos.fdpn_fecha_ult_act.getOrElse(""))
            val telefonoEmpresa: String = Option(datos.datosEmp.fdpn_tele_emp).getOrElse("")
            datos.copy(fdpn_fecha_ult_act = fechaActualizacion, datosEmp = datos.datosEmp.copy(fdpn_tele_emp = telefonoEmpresa))
        }
      case _ => Future.failed(ValidacionException("409.15", "Tipo de usuario no permitido"))
    }
  }

  def comprobarDatos(user: UsuarioAuth): Future[Boolean] = {
    for {
      datos <- obtenerDatos(user)
    } yield {
      //obtener fecha string
      val fechaString: String = datos.fdpn_fecha_ult_act.getOrElse("1990-01-01 00:00:00")
      //Obtener fecha actualizacion
      val format = new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
      val fechaActualizacion = new DateTime(format.parse(fechaString).getTime)
      //Obtener fecha comparacion
      val fechaComparacion = new DateTime().minusYears(1).minusDays(1)
      //si la fecha es antes (true), entoces debe actualizar datos
      fechaComparacion.isAfter(fechaActualizacion.getMillis)
    }
  }

  def obtenerCiudades(pais: Int): Future[Seq[Ciudad]] = {
    transformarString[Seq[Ciudad]](actualizacionDAO.listarCiudades(pais), DataAccessTranslator.translateCiudades)
  }

  def obtenerEnviosCorrespondencia(): Future[Seq[EnvioCorrespondencia]] = {
    transformarString[Seq[EnvioCorrespondencia]](actualizacionDAO.listarEnvioCorrespondencia, DataAccessTranslator.translateEnviosCorrespondencia)
  }

  def obtenerActividadesEconomicas(): Future[Seq[ActividadEconomica]] = {
    transformarString[Seq[ActividadEconomica]](actualizacionDAO.listarActividadesEconomicas, DataAccessTranslator.translateActividadesEconomicas)
  }

  def actualizarDatos(user: UsuarioAuth, msg: ActualizacionMessage): Future[String] = {
    val tipoIdAlianza: String = TiposIdentificacionCore.getTipoIdentificacion(user.tipoIdentificacion)
    actualizacionDAO.actualizarCliente(user.identificacion, tipoIdAlianza, msg.fdpn_nombre1, msg.fdpn_nombre2, msg.fdpn_apell1, msg.fdpn_apell2,
      msg.fdpn_pais_residencia, msg.fdpn_drcl_dire_res, msg.fdpn_drcl_dire_ofi, msg.fdpn_drcl_ciud_res, msg.fdpn_drcl_tele_res, msg.fdpn_dcfd_email,
      msg.fdpn_dcfd_email_ant, msg.fdpn_dcfd_tipo, msg.fdpn_dcfd_tipo_ant, msg.fdpn_envio_corresp, msg.fdpn_telefono_movil_1, msg.fdpn_pais_tel_mov_1,
      msg.fdpn_ocupacion, msg.datosEmp.fdpn_if_declara_renta, msg.datosEmp.fdpn_pafd_pais, msg.datosEmp.fdpn_pafd_pais_ant, msg.fdpn_ciua,
      msg.datosEmp.fdpn_nombre_emp, msg.datosEmp.fdpn_nit_emp, msg.datosEmp.fdpn_cargo, msg.datosEmp.fdpn_dire_emp, msg.datosEmp.fdpn_ciud_emp,
      msg.datosEmp.fdpn_ciud_nombre_emp, msg.datosEmp.fdpn_tele_emp, msg.datosEmp.fdpn_if_vactivos, msg.datosEmp.fdpn_if_vpasivos,
      msg.datosEmp.fdpn_if_vpatrimonio, msg.datosEmp.fdpn_if_vingresos, msg.datosEmp.fdpn_if_vegresos, msg.datosEmp.fdpn_if_vingresos_noop_mes)
  }

  private def transformarString[T](futureString: Future[String], f: String => T): Future[T] = {
    for {
      jsString <- futureString
    } yield f(jsString)
  }

}
