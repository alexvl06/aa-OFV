package co.com.alianza.infrastructure.dto

/**
 *
 * @author seven4n
 */
case class Pregunta(id: Int, pregunta: String)
case class Respuesta(idPregunta: Int, respuesta: String)
case class RespuestaCompleta(idPregunta: Int, pregunta: String, respuesta: String)
