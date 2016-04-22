package co.com.alianza.infrastructure.anticorruption.recursosAgenteEmpresarial

import co.com.alianza.infrastructure.dto.RecursoPerfilAgente
import co.com.alianza.persistence.entities.{ RecursoPerfilAgente => eRecursoPerfilAgente }

/**
 *
 * @author seven4n
 */
object DataAccessTranslator {

  def translate(recursos: List[eRecursoPerfilAgente]) =
    recursos map (recurso => RecursoPerfilAgente(recurso.idPerfil, recurso.urlRecurso, recurso.acceso, recurso.filtro))

}
