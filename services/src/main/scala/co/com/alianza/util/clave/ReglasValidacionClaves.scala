package co.com.alianza.util.clave

import co.com.alianza.infrastructure.anticorruption.contrasenas.DataAccessAdapter
import scala.concurrent.{ExecutionContext, Future}
import scalaz.Validation
import co.com.alianza.exceptions.PersistenceException
import scalaz.{Success => zSuccess}
import co.com.alianza.app.MainActors
import java.util.regex.Pattern


abstract sealed class Regla(val name:String) {
  def validar(valor:String, condicion:Option[String]):Option[ErrorValidacionClave]
}

case object MinCaracteres extends Regla("MINIMO_NUMERO_CARACTERES")  {

  def validar(valor:String, condicion:Option[String]):Option[ErrorValidacionClave] = {
    condicion match {
      case Some(value) => if (valor.length < value.toInt)  Some(ErrorMinCaracteres) else None
      case None => Some(ErrorMinCaracteres)
    }
  }

}

case object MinCaracteresEspeciales extends Regla("MINIMO_NUMERO_CARACTERES_ESPECIALES")  {

   def validar(valor:String, condicion:Option[String]):Option[ErrorValidacionClave] = {
     condicion match {
       case Some(value) =>
         val pattern = s"""(?=(?:.*?[^a-zA-Z0-9]){$value})""".r
         pattern findFirstIn  valor match {
            case Some(_) => None
            case _ => Some(ErrorMinCaracteresEsp)
         }
       case None => Some(ErrorMinCaracteresEsp)
     }

   }
}

case object MinNumDigitos extends Regla("MINIMO_NUMERO_DIGITOS")  {

  def validar(valor:String, condicion:Option[String]):Option[ErrorValidacionClave] = {
    condicion match {
      case Some(value) =>
        val pattern = s"""(?=(?:.*?[0-9]){$value})""".r
        pattern findFirstIn  valor match {
          case Some(_) => None
          case _ => Some(ErrorMinDigitos)
        }
      case None => Some(ErrorMinDigitos)
    }

  }
}

case object MinMayusculas extends Regla("MINIMO_NUMERO_LETRAS_MAYUSCULAS")  {

  def validar(valor:String, condicion:Option[String]):Option[ErrorValidacionClave] = {
    condicion match {
      case Some(value) =>
        val pattern = s"""(?=(?:.*?[A-Z]){$value})""".r
        pattern findFirstIn  valor match {
          case Some(_) => None
          case _ => Some(ErrorMinMayusculas)
        }
      case None => Some(ErrorMinMayusculas)
    }

  }
}

case object MinMinusculas extends Regla("MINIMO_NUMERO_LETRAS_MINUSCULAS")  {

  def validar(valor:String, condicion:Option[String]):Option[ErrorValidacionClave] = {
    condicion match {
      case Some(value) =>
        val pattern =  s"""(?=(?:.*?[a-z]){$value})""".r
        pattern findFirstIn  valor match {
          case Some(_) => None
          case _ => Some(ErrorMinMinusculas)
        }
      case None => Some(ErrorMinMinusculas)
    }

  }
}

case object CaracteresPermitidos extends Regla("CARACTERES_PERMITIDOS")  {

  def validar(valor:String, condicion:Option[String]):Option[ErrorValidacionClave] = {

    condicion match {
      case Some(value) =>
        val pattern = s"""(?=(?:.*?[^a-zA-Z0-9$value]))"""".r
        pattern findFirstIn  valor match {
          case Some(_) => Some(ErrorCaracteresPermitidos)
          case _ => None
        }
      case None => Some(ErrorCaracteresPermitidos)
    }

  }
}


case object IntentosIngresoContrasena extends Regla("CANTIDAD_REINTENTOS_INGRESO_CONTRASENA")  {

  def validar(valor:String, condicion:Option[String]):Option[ErrorValidacionClave] = {
    condicion match {
      case Some(value) => if (valor.toInt == value.toInt)  Some(ErrorIntentosErroneos) else None
      case None => Some(ErrorIntentosErroneos)
    }
  }

}


case object CambioContrasena extends Regla("DIAS_VALIDA")  {

  def validar(valor:String, condicion:Option[String]):Option[ErrorValidacionClave] = {
    condicion match {
      case Some(value) => if (valor.toInt == value.toInt)  Some(ErrorIntentosErroneos) else None
      case None => Some(ErrorIntentosErroneos)
    }
  }

}




object ValidarClave {

  implicit val ec: ExecutionContext = MainActors.dataAccesEx

  def aplicarReglas(input:String, validaciones:Regla* ) = {
    obtenerReglasToMap.map(_.flatMap{
         f => zSuccess(
              validaciones.foldLeft(Nil:List[ErrorValidacionClave]){
                 (acc : List[ErrorValidacionClave], r: Regla) => r.validar(input,f.get(r.name)).map(_ :: acc).getOrElse(acc)
            }
         )
     })
  }

  private def obtenerReglasToMap: Future[Validation[PersistenceException, Map[String, String]]] = {
    //val reglasFuture = DataAccessAdapter.consultarReglasContrasenas().map(_.leftMap(pe => ErrorObteniendoReglas))
    val reglasFuture = DataAccessAdapter.consultarReglasContrasenas()
    reglasFuture.map(_.flatMap(list => zSuccess(list.map( x => (x.llave, x.valor)) toMap)))

  }

  def reglasGenerales = List(MinCaracteres, MinCaracteresEspeciales,MinNumDigitos,MinMayusculas,MinMinusculas,CaracteresPermitidos)

  //TODO:CambioContrasena Falta realizar la validacion de la fecha sumando los días establecidos en la DB
  def reglasIngresoUsuario = List( IntentosIngresoContrasena, CambioContrasena )

}