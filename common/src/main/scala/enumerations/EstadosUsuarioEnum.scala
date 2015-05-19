package enumerations

object EstadosUsuarioEnum extends Enumeration(0) {

  type estadoUsuario = Value

  val bloqueContraseña      = Value(0)
  val activo                = Value(1)
  val pendienteActivacion   = Value(2)
  val pendienteReinicio     = Value(4)

}
