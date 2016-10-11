package portal.transaccional.autenticacion.service.drivers.actualizacion

import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.exceptions.ValidacionException
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import enumerations.TiposIdentificacionCore
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
        transformarString[DatosCliente](futuroString, DataAccessTranslator.translateDatosCliente)
      case _ => Future.failed(ValidacionException("409.15", "Tipo de usuario no permitido"))
    }
  }

  def comprobarDatos(): Future[Boolean] = {
    //TODO: implementar
    Future.failed(new NotImplementedError())
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

  def actualizarDatos(): Future[Boolean] = {
    //TODO: implementar
    Future.failed(new NotImplementedError())
  }

  private def transformarString[T](futureString: Future[String], f: String => T): Future[T] = {
    for {
      jsString <- futureString
    } yield f(jsString)
  }

}
