package portal.transaccional.autenticacion.service.drivers.autorizacion

import co.com.alianza.exceptions.NoAutorizado
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.ServicioComercialDAOs

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by dfbaratov on 23/08/16.
 */
case class AutorizacionServicioComercialDriverRepository(servicioComercialDAO: ServicioComercialDAOs)(implicit val ex: ExecutionContext) extends AutorizacionServicioComercialRepository {

  override def estaAutorizado(rolId: Int, url: String): Future[Boolean] = {
    val retorno = for {
      existe <- servicioComercialDAO.existe(url)
      autorizado <- servicioComercialDAO.autorizadoServicio(rolId, url)
    } yield (autorizado.isDefined || !existe)
    retorno flatMap {
      case true => retorno
      case false => Future.failed(NoAutorizado("Servicio restringido por recurso comercial"))
    }
  }
}
