package co.com.alianza.commons.enumerations

object TiposCliente extends Enumeration {

  type TiposCliente = Value

  val clienteIndividual, agenteEmpresarial, clienteAdministrador, comercialAdmin, comercialFiduciaria, comercialValores = Value

  def getTipoCliente(tipoCliente: String): TiposCliente = {
    this.values.filter(tipo => tipo.toString.equals(tipoCliente)).head
  }

}
