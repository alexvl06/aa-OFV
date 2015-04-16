package co.com.alianza.commons.enumerations

/**
 * Created by manuel on 8/01/15.
 */
object TiposTransacciones extends Enumeration {

  case class TipoTransaccion(codigo: Int, nombre: String) extends Val(codigo, nombre)

  val CONSULTA = TipoTransaccion( 1 ,"Consulta" )
  val `INSCRIPCIÓN` = TipoTransaccion( 2 , "Inscripción" )
  val `PAGOS MASIVOS` = TipoTransaccion( 3 , "Pagos Masivos" )
  val `TRANSFERENCIAS Y RETIROS` = TipoTransaccion( 4 ,"Transferencias y Retiros" )

}
