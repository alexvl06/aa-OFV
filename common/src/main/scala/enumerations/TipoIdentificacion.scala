package enumerations

/**
 * Created by josegarcia on 10/06/14.
 */
object TipoIdentificacion extends Enumeration {


  protected case class Val( identificador:Int, name: String ) extends super.Val(nextId, name) {

  }

  val CEDULA_CUIDADANIA = Val( 1 ,"CC" )
  val CEDULA_EXTRANJERIA = Val( 2 , "CE" )
  val NIT = Val( 3 , "NIT" )



}
