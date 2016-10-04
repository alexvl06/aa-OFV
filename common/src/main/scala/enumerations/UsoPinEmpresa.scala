package enumerations

/**
 * Created by S4N on 29/12/14.
 */
object UsoPinEmpresaEnum extends Enumeration(0) {

  type usoPinEmpresa = Value

  val usoReinicioContrasena = Value("reinicio de contrasena") //Valor id = 0
  val creacionAgenteEmpresarial = Value("creación agente empresarial") //Valor id = 1
  val creacionAgenteInmobiliario = Value("creación agente inmobiliario") //Valor id = 2

}
