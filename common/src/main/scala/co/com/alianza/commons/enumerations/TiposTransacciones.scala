package co.com.alianza.commons.enumerations

/**
 * Created by manuel on 8/01/15.
 */
object TiposTransacciones extends Enumeration {

  case class TipoTransaccion(codigo: Int, nombre: String) extends Val(codigo, nombre)

  val CONSULTA = TipoTransaccion( 1 ,"Consulta" )
  val `MATRíCULA` = TipoTransaccion( 2 , "Matrícula" )
  val `DISPERSIÓN DE PAGOS` = TipoTransaccion( 3 , "Dispersión de pagos" )
  val TRANSFERENCIA = TipoTransaccion( 4 ,"Transferencia" )

}
