package enumerations

import scala.collection.immutable.HashMap

/**
 * Created by hernando on 17/07/15.
 */
object TiposIdentificacionCore {

  val mapaTiposIdentificacionAlianza: HashMap[Int, String] = HashMap[Int, String](
    1 -> "C", // CedulaCiudadania
    2 -> "E", // CedulaExtranjeria
    3 -> "A", // Nit
    4 -> "F", // Fideicomiso
    5 -> "T", // TarjetaIdentidad
    6 -> "P", // Pasaporte
    7 -> "R", //RegistroCivil
    8 -> "X" //SocExtrsinNitEnColombia
  )

  def getTipoIdentificacion(tipoIdentificacion: Int): String = mapaTiposIdentificacionAlianza.get(tipoIdentificacion).getOrElse("")
}