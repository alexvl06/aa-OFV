package co.com.alianza.persistence.entities

import CustomDriver.simple._

/**
 * Created by seven4n
 */

case class PreguntasConfrontacionUsuario (idPregunta: Int, idUsuario: Int, respuesta: String)

class PreguntasConfrontacionUsuarioTable (tag: Tag) extends Table[PreguntasConfrontacionUsuario](tag, "PREGUNTAS_CONFRONTACION_USUARIO") {

  def idEmpresa = column[Int]("ID_EMPRESA")
  def idUsuario = column[Int]("ID_USUARIO")
  def respuesta = column[String]("RESPUESTA")

  def * = (idEmpresa, idUsuario, respuesta) <> (PreguntasConfrontacionUsuario.tupled, PreguntasConfrontacionUsuario.unapply)

}
