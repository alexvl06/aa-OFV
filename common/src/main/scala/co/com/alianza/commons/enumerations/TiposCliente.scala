package co.com.alianza.commons.enumerations

object TiposCliente extends Enumeration {

  type TiposCliente = Value

  val clienteIndividual = Value(0)
  val agenteEmpresarial = Value(1)
  val clienteAdministrador = Value(2)

  private val tipos = List(clienteIndividual, agenteEmpresarial, clienteAdministrador)

  implicit val deId: Int => TiposCliente = id => tipos filter { _.id == id } head
}
