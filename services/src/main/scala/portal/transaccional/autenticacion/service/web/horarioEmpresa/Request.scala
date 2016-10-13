package portal.transaccional.autenticacion.service.web.horarioEmpresa

/**
 * Created by jonathan on 12/10/16.
 */
case class AgregarHorarioEmpresaRequest(diaHabil: Boolean, sabado: Boolean, horaInicio: String, horaFin: String)

case class DiaFestivoRequest(fecha: String)