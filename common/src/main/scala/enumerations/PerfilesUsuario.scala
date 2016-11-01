package enumerations

/**
 *
 * @author smontanez
 */
object PerfilesUsuario extends Enumeration(1) {

  type perfilUsuario = Value

  val clienteIndividual = Value("cliente individual") //valor id = 1
  val clienteAdministrador = Value("agente empresarial") //valor id = 2
  val agenteEmpresarial = Value("agente empresarial") //valor id = 3

}

object PerfilesAgente extends Enumeration(1) {

  type perfilUsuario = Value

  val agente = Value("Agente") //valor id = 1

}

object PerfilInmobiliarioEnum extends Enumeration(1) {

  type perfilInmobiliario = Value

  val agente = Value("AGENTE") //valor id = 1
  val admin = Value("ADMIN") //valor id = 2

}