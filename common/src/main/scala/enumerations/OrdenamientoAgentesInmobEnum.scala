package enumerations

object OrdenamientoAgentesInmobEnum extends Enumeration(0) {

  type OrdenamientoAgentesInmobEnum = Value

  val ID = Value("ID")
  val NOMBRE = Value("NM")
  val USUARIO = Value("US")
  val CORREO = Value("EM")
  val ESTADO = Value("ES")
  val ESTADO_PENDIENTE_ACTIVACION = Value("ES:PA")
}
