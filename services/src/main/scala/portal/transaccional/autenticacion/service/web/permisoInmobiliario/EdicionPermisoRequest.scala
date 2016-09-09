package portal.transaccional.autenticacion.service.web.permisoInmobiliario


case class EdicionPermisoRequest (fideicomiso : String, agentesInmobiliarios: List[String], permisos: List[PermisoInmobiliario],  proyectos : List[String])
