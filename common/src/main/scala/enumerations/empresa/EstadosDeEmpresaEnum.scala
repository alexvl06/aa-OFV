package enumerations.empresa

/**
 * Created by S4N on 16/02/15.
 */
object EstadosDeEmpresaEnum extends Enumeration(0) {

  type estadoUsuario = Value

  val inactiva = Value("Inactiva")
  val activa = Value("Activa")

}
