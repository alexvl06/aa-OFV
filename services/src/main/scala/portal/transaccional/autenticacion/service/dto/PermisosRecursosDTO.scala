package portal.transaccional.autenticacion.service.dto

/**
 * Created by dfbaratov on 6/09/16.
 */
case class RecursoDTO(idRecurso: Int, roles: List[Int])

case class PermisoRecursoDTO(recursos: List[RecursoDTO])
