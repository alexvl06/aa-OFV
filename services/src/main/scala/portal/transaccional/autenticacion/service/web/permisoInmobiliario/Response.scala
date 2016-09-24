package portal.transaccional.autenticacion.service.web.permisoInmobiliario

case class ConsultarAgenteInmobiliarioResponse(id: Int,
                                               correo: String,
                                               usuario: String,
                                               estado: Int,
                                               nombre: Option[String],
                                               cargo: Option[String],
                                               descripcion: Option[String])

case class ConsultarAgenteInmobiliarioListResponse(_metadata: PaginacionMetadata,
                                                    agentes: Seq[ConsultarAgenteInmobiliarioResponse])

case class PaginacionMetadata(pagina: Int,
                              itemsPorPagina: Int,
                              totalPagina: Int,
                              totalItems: Int)