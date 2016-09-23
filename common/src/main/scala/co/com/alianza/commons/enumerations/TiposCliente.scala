package co.com.alianza.commons.enumerations

object TiposCliente extends Enumeration {

  type TiposCliente = Value

  val clienteIndividual, //0
  agenteEmpresarial, //1
  clienteAdministrador, //2
  comercialAdmin, //3
  comercialFiduciaria, //4
  comercialValores, // 5
  comercialSAC = Value //6

  def getTipoCliente(tipoCliente: String): TiposCliente = {
    this.values.filter(tipo => tipo.toString.equals(tipoCliente)).head
  }

}
