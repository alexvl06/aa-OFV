package co.com.alianza.persistence.messages


/**
 * Created by ricardoseven on 16/06/14.
 */
case class ObtenerCuestionarioRequest(codigoCiudad:Int, codigoCuestionario:Int, codigoDepartamento:Int, codigoTipoIdentificacion:String, numeroIdentificacion: String, telefono:String)

case class ObtenerCuestionarioAdicionalRequest(codigoCiudad:Int, codigoCuestionario:Int, codigoDepartamento:Int, codigoTipoIdentificacion:String, numeroIdentificac: String, telefono:String, secuenciaCuestionario:Long)

case class ValidarCuestionarioRequest(codigoCuestionario:Int,respuestas:String ,secuenciaCuestionario:Int)