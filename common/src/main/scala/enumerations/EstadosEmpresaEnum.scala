package enumerations

/**
 * Created by S4N on 22/12/14.
 */
object EstadosEmpresaEnum extends Enumeration(0) {

  type estadoEmpresa = Value

  val bloqueContraseña = Value("Bloqueado por Contraseña")
  val activo = Value("Activo")
  val pendienteActivacion = Value("Pendiente de Activación")
  val pendienteReiniciarContrasena = Value("Pendiente de Reinicio de Contraseña")
  val bloqueadoPorAdmin = Value("Bloqueado por Administrador")

}
