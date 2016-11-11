package co.com.alianza.commons.enumerations

object TiposCliente extends Enumeration {

  type TiposCliente = Value

  val clienteIndividual, //0
  agenteEmpresarial, //1
  clienteAdministrador, //2
  comercialAdmin, //3
  comercialFiduciaria, //4
  comercialValores, // 5
  comercialSAC, // 6
  clienteAdminInmobiliario, // 7
  agenteInmobiliario = Value // 8

  def getTipoCliente(tipoCliente: String): TiposCliente = {
    this.values.filter(tipo => tipo.toString.equals(tipoCliente)).head
  }

  private val tiposIdIndividual: List[Int] = List(
    1, // CedulaCiudadania
    2, // CedulaExtranjeria
    5, // TarjetaIdentidad
    8
  ) // RegistroCivil

  def tipoClientePorTipoIdentificacion(tipoIdentificacion: Int): TiposCliente = {
    if (tiposIdIndividual.contains(tipoIdentificacion)) {
      clienteIndividual
    } else {
      clienteAdministrador
    }
  }

}
