package enumerations

object TipoAgenteInmobiliario extends Enumeration(1) {

  type TipoAgenteInmobiliario = Value

  val internoAdmin = Value("A")
  val internoAgente = Value("I")
  val empresarial = Value("E")

}
