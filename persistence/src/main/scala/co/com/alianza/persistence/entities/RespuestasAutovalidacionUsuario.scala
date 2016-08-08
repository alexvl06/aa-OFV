package co.com.alianza.persistence.entities

import CustomDriver.simple._

/**
 * Created by seven4n
 */

case class RespuestasAutovalidacionUsuario(idPregunta: Int, idUsuario: Int, respuesta: String)

class RespuestasAutovalidacionUsuarioTable(tag: Tag) extends Table[RespuestasAutovalidacionUsuario](tag, "RESPUESTAS_AUTOVALIDACION_USUARIO") {
  def idPregunta = column[Int]("ID_PREGUNTA")
  def idUsuario = column[Int]("ID_USUARIO")
  def respuesta = column[String]("RESPUESTA")
  def * = (idPregunta, idUsuario, respuesta) <> (RespuestasAutovalidacionUsuario.tupled, RespuestasAutovalidacionUsuario.unapply)
}

class RespuestasAutovalidacionUsuarioAdministradorTable(tag: Tag) extends Table[RespuestasAutovalidacionUsuario](tag, "RESPUESTAS_AUTOVALIDACION_USUARIO_EMPRESARIAL_ADMIN") {
  def idPregunta = column[Int]("ID_PREGUNTA")
  def idUsuario = column[Int]("ID_USUARIO")
  def respuesta = column[String]("RESPUESTA")
  def * = (idPregunta, idUsuario, respuesta) <> (RespuestasAutovalidacionUsuario.tupled, RespuestasAutovalidacionUsuario.unapply)
}
