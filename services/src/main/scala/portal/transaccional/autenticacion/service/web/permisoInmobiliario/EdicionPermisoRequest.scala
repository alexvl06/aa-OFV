package portal.transaccional.autenticacion.service.web.permisoInmobiliario

import co.com.alianza.commons.enumerations.TipoPermisoInmobiliario.TipoPermisoInmobiliario

case class EdicionPermisoRequest(fideicomiso: Int, agentesInmobiliarios: Seq[Int], permisos: Seq[TipoPermisoInmobiliario], proyectos: Seq[Int])
