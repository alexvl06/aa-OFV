package portal.transaccional.autenticacion.service.web.permisoInmobiliario

case class ConsultarAgenteInmobiliarioResponse(id: Int,
                                               correo: String,
                                               usuario: String,
                                               nombre: Option[String],
                                               cargo: Option[String],
                                               descripcion: Option[String])
