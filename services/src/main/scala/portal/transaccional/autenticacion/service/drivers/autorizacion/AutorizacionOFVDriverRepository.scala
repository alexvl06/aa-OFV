package portal.transaccional.autenticacion.service.drivers.autorizacion

import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.util.ConfigApp
import com.typesafe.config.Config
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.AlianzaDAO

import scala.concurrent.{ ExecutionContext, Future }

case class AutorizacionOFVDriverRepository(alianzaDAO: AlianzaDAO)(implicit val ex: ExecutionContext) extends AutorizacionOFVRepository {
  //tipos clientes
  private val config: Config = ConfigApp.conf

  val agente = TiposCliente.agenteEmpresarial.toString
  val admin = TiposCliente.clienteAdministrador.toString
  val individual = TiposCliente.clienteIndividual.toString
  val adminInmobiliaria = TiposCliente.clienteAdminInmobiliario.toString
  val agenteInmobiliario = TiposCliente.agenteInmobiliario.toString

  def validar(token: String, tipoCliente: String): Future[Boolean] = {
    val resultado = tipoCliente match {
      case `agente` => validarAgente(token)
      case `admin` | `adminInmobiliaria` => validarAdmin(token)
      case `individual` => validarIndividual(token)
      case `agenteInmobiliario` => validarAgenteInmobiliario(token)
    }
    resultado
  }

  def validarAgente(token: String): Future[Boolean] = {
    for {
      usuario <- alianzaDAO.getByTokenAgente(token)
      existe <- identificacionesConfiguradas(usuario._1.identificacion, "ofv.tester.agente.ids")
    } yield existe
  }

  def validarAdmin(token: String): Future[Boolean] = {
    for {
      usuario <- alianzaDAO.getByTokenAdmin(token)
      existe <- identificacionesConfiguradas(usuario._1.identificacion, "ofv.tester.admin.ids")
    } yield existe
  }

  def validarIndividual(token: String): Future[Boolean] = {
    for {
      usuario <- alianzaDAO.getByTokenUsuario(token)
      existe <- identificacionesConfiguradas(usuario.get.identificacion, "ofv.tester.natural.ids")
    } yield existe
  }

  def validarAgenteInmobiliario(token: String): Future[Boolean] = {
    for {
      usuario <- alianzaDAO.getByTokenAgenteInmobiliario(token)
      existe <- identificacionesConfiguradas(usuario.identificacion, "ofv.tester.agenteinmo.ids")
    } yield existe
  }

  def identificacionesConfiguradas(identificacion: String, propiedad: String): Future[Boolean] = {
    val identificaciones = config.getString(propiedad).split(",").filter(_.equalsIgnoreCase(identificacion))
    Future.successful(identificaciones.length > 0)
  }
}
