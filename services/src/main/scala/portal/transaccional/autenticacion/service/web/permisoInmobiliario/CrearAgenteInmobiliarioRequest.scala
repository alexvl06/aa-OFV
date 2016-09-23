package portal.transaccional.autenticacion.service.web.permisoInmobiliario

import scala.util.matching.Regex

case class CrearAgenteInmobiliarioRequest(correo: String,
                                          usuario: String,
                                          nombre: Option[String],
                                          cargo: Option[String],
                                          descripcion: Option[String]) {
  require(!correo.isEmpty, "Field correo cannot be empty")
  require(isValidEmail(correo), "Field email is not a valid email address")
  require(!usuario.isEmpty, "Field usuario cannot be empty")

  nombre.foreach(x => require(!x.isEmpty, "Field nombre cannot be empty"))
  cargo.foreach(x => require(!x.isEmpty, "Field cargo cannot be empty"))
  descripcion.foreach(x => require(!x.isEmpty, "Field descripcion cannot be empty"))

  def isValidEmail(email: String): Boolean = {
    val emailRegex: Regex =
      """^[a-zA-Z0-9\.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$""".r
    emailRegex.findFirstMatchIn(email).isDefined
  }
}
