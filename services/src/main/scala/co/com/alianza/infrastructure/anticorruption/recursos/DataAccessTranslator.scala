package co.com.alianza.infrastructure.anticorruption.recursos

import co.com.alianza.infrastructure.dto.RecursoUsuario
import co.com.alianza.persistence.entities.{ RecursoPerfil => eRecursoPerfil }

/**
 *
 * @author seven4n
 */
object DataAccessTranslator {

  def translate(recursos: Seq[eRecursoPerfil]) = {
    recursos.map(recurso => RecursoUsuario(recurso.idPerfil, recurso.urlRecurso, recurso.acceso, recurso.filtro)).toList
  }

}
