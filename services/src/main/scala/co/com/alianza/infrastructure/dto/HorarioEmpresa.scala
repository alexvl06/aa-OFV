package co.com.alianza.infrastructure.dto

import java.sql.Time

case class HorarioEmpresa(diaHabil: Boolean, sabado: Boolean, horaInicio: Time, horaFin: Time)
