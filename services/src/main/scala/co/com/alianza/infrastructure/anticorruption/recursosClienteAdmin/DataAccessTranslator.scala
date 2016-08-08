package co.com.alianza.infrastructure.anticorruption.recursosClienteAdmin

import co.com.alianza.infrastructure.dto.RecursoPerfilClienteAdmin
import co.com.alianza.persistence.entities.{ RecursoPerfilClienteAdmin => eRecursoPerfilClienteAdmin }

/**
 * Created by manuel on 3/02/15.
 */
object DataAccessTranslator {

  def translate(recursos: Seq[eRecursoPerfilClienteAdmin]) =
    recursos.map(recurso => RecursoPerfilClienteAdmin(recurso.idPerfil, recurso.urlRecurso, recurso.acceso, recurso.filtro)).toList

}
