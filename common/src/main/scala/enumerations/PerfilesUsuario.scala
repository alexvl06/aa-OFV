package enumerations

/**
 *
 * @author smontanez
 */
object PerfilesUsuario extends Enumeration(1) {

    type perfilUsuario = Value

    val clienteIndividual = Value("cliente individual") //valor id = 1
    val clienteAdministrador = Value("agente empresarial") //valor id = 2
    val agenteEmpresarial= Value("agente empresarial") //valor id = 3

}