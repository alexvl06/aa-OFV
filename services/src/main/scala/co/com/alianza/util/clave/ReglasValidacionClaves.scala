package co.com.alianza.util.clave

import java.util.regex.Pattern

import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.anticorruption.contrasenas.DataAccessAdapter
import co.com.alianza.infrastructure.anticorruption.ultimasContrasenas.{ DataAccessAdapter => DataAccessAdapterUltimaContrasena }
import co.com.alianza.infrastructure.anticorruption.ultimasContrasenasAgenteEmpresarial.{ DataAccessAdapter => DataAccessAdapterUltimaContrasenaAgenteE }
import co.com.alianza.infrastructure.anticorruption.ultimasContrasenasClienteAdmin.{ DataAccessAdapter => DataAccessAdapterUltimaContrasenaClienteAdmin }
import co.com.alianza.persistence.entities.UltimaContrasena
import enumerations.{ AppendPasswordUser, PerfilesUsuario }
import portal.transaccional.autenticacion.service.drivers.reglas._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ Await, Future }
import scalaz.Validation.FlatMap._
import scalaz.{ Validation, Failure => zFailure, Success => zSuccess }

abstract sealed class Regla(val name: String) {
  def validar(valor: String, idUsuario: Option[Int], perfilUsuario: PerfilesUsuario.perfilUsuario, condicion: Option[String]): Option[ValidacionClave]
}

case object MinCaracteres extends Regla("MINIMO_NUMERO_CARACTERES") {

  def validar(valor: String, idUsuario: Option[Int], perfilUsuario: PerfilesUsuario.perfilUsuario, condicion: Option[String]): Option[ValidacionClave] = {
    condicion match {
      case Some(value) => if (valor.length < value.toInt) Some(ErrorMinCaracteres) else None
      case None => Some(ErrorMinCaracteres)
    }
  }

}

case object MinCaracteresEspeciales extends Regla("MINIMO_NUMERO_CARACTERES_ESPECIALES") {

  def validar(valor: String, idUsuario: Option[Int], perfilUsuario: PerfilesUsuario.perfilUsuario, condicion: Option[String]): Option[ValidacionClave] = {
    condicion match {
      case Some(value) =>
        val pattern = s"""(?=(?:.*?[^a-zA-Z0-9]){$value})""".r
        pattern findFirstIn valor match {
          case Some(_) => None
          case _ => Some(ErrorMinCaracteresEsp)
        }
      case None => Some(ErrorMinCaracteresEsp)
    }

  }
}

case object MinNumDigitos extends Regla("MINIMO_NUMERO_DIGITOS") {

  def validar(valor: String, idUsuario: Option[Int], perfilUsuario: PerfilesUsuario.perfilUsuario, condicion: Option[String]): Option[ValidacionClave] = {
    condicion match {
      case Some(value) =>
        val pattern = s"""(?=(?:.*?[0-9]){$value})""".r
        pattern findFirstIn valor match {
          case Some(_) => None
          case _ => Some(ErrorMinDigitos)
        }
      case None => Some(ErrorMinDigitos)
    }

  }
}

case object MinMayusculas extends Regla("MINIMO_NUMERO_LETRAS_MAYUSCULAS") {

  def validar(valor: String, idUsuario: Option[Int], perfilUsuario: PerfilesUsuario.perfilUsuario, condicion: Option[String]): Option[ValidacionClave] = {
    condicion match {
      case Some(value) =>
        val pattern = s"""(?=(?:.*?[A-Z]){$value})""".r
        pattern findFirstIn valor match {
          case Some(_) => None
          case _ => Some(ErrorMinMayusculas)
        }
      case None => Some(ErrorMinMayusculas)
    }

  }
}

case object MinMinusculas extends Regla("MINIMO_NUMERO_LETRAS_MINUSCULAS") {

  def validar(valor: String, idUsuario: Option[Int], perfilUsuario: PerfilesUsuario.perfilUsuario, condicion: Option[String]): Option[ValidacionClave] = {
    condicion match {
      case Some(value) =>
        val pattern = s"""(?=(?:.*?[a-z]){$value})""".r
        pattern findFirstIn valor match {
          case Some(_) => None
          case _ => Some(ErrorMinMinusculas)
        }
      case None => Some(ErrorMinMinusculas)
    }

  }
}

case object CaracteresPermitidos extends Regla("CARACTERES_PERMITIDOS") {

  def validar(valor: String, idUsuario: Option[Int], perfilUsuario: PerfilesUsuario.perfilUsuario, condicion: Option[String]): Option[ValidacionClave] = {

    condicion match {
      case Some(value) =>

        val valueFormat = Pattern.quote(value).replaceAll("-", "\\-")
        val pattern = Pattern.compile(s"""(?=(?:.*?[^a-zA-Z0-9$valueFormat]))""")
        val m = pattern.matcher(valor)
        val b = m.find()
        b match {
          case true => Some(ErrorCaracteresPermitidos)
          case false => None
        }

      case None => Some(ErrorCaracteresPermitidos)
    }

  }
}

case object IntentosIngresoContrasena extends Regla("CANTIDAD_REINTENTOS_INGRESO_CONTRASENA") {

  def validar(valor: String, idUsuario: Option[Int], perfilUsuario: PerfilesUsuario.perfilUsuario, condicion: Option[String]): Option[ValidacionClave] = {
    condicion match {
      case Some(value) => if (valor.toInt == value.toInt) Some(ErrorIntentosErroneos) else None
      case None => Some(ErrorIntentosErroneos)
    }
  }

}

case object CambioContrasena extends Regla("DIAS_VALIDA") {

  def validar(valor: String, idUsuario: Option[Int], perfilUsuario: PerfilesUsuario.perfilUsuario, condicion: Option[String]): Option[ValidacionClave] = {
    condicion match {
      case Some(value) => if (valor.toInt == value.toInt) Some(ErrorIntentosErroneos) else None
      case None => Some(ErrorIntentosErroneos)
    }
  }

}

case object UltimasContrasenas extends Regla("ULTIMAS_CONTRASENAS_NO_VALIDAS") {

  import scala.concurrent.duration._

  def validar(valorContrasenaNueva: String, idUsuario: Option[Int], perfilUsuario: PerfilesUsuario.perfilUsuario, condicion: Option[String]): Option[ValidacionClave] = {
    condicion match {
      case Some(value) =>
        idUsuario match {
          case Some(valueIdUsuario) =>
            val FuturoObtenerUltimasContrasenas: (Future[Validation[PersistenceException, Seq[Any]]]) = perfilUsuario match {
              case PerfilesUsuario.clienteIndividual => DataAccessAdapterUltimaContrasena.obtenerUltimasContrasenas(value, idUsuario.get)
              case PerfilesUsuario.agenteEmpresarial => DataAccessAdapterUltimaContrasenaAgenteE.obtenerUltimasContrasenas(value, idUsuario.get)
              case PerfilesUsuario.clienteAdministrador => DataAccessAdapterUltimaContrasenaClienteAdmin.obtenerUltimasContrasenas(value, idUsuario.get)
            }

            val UltimaContrasenaExiste: Future[Validation[PersistenceException, Boolean]] = FuturoObtenerUltimasContrasenas.map {
              validationInterior =>
                validationInterior.map { listaUltimasContrasenas => contiene(idUsuario.get, listaUltimasContrasenas, valorContrasenaNueva) }
            }
            //TODO: AWAIT ?? DE VERDAD ??
            val extraccionFuturo = Await.result(UltimaContrasenaExiste, 8.seconds)

            extraccionFuturo match {
              case zSuccess(responseBol) =>
                if (responseBol) Some(ErrorUltimasContrasenas) else None
              case zFailure(error) => Some(ErrorUltimasContrasenas)
            }

          case None => Some(ErrorUltimasContrasenas)
        }

      case None => Some(ErrorUltimasContrasenas)
    }
  }

  def contiene(idUsuario: Int, lista: Seq[Any], valorContrasenaNueva: String): Boolean = {
    val contrasenaNuevaConSalt: String = valorContrasenaNueva.concat(AppendPasswordUser.appendUsuariosFiducia)
    val contrasenaNuevaHash: String = Crypto.hashSha512(contrasenaNuevaConSalt, idUsuario)

    def compare(contrasena: String, contrasenaNuevaHash: String): Boolean = {
      contrasena == contrasenaNuevaHash
    }

    val listaResultadosComparacionContrasena: Seq[Boolean] = lista.map {
      case UltimaContrasena(param1, param2, param3, param4) => compare(param3, contrasenaNuevaHash)
    }
    listaResultadosComparacionContrasena.contains(elem = true)
  }

}

object ValidarClave {

  def aplicarReglas(input: String, idUsuario: Option[Int], perfilUsuario: PerfilesUsuario.perfilUsuario,
    validaciones: Regla*): Future[Validation[PersistenceException, List[ValidacionClave]]] = {
    obtenerReglasToMap.map(_.flatMap { f =>
      zSuccess(
        validaciones.foldLeft(Nil: List[ValidacionClave]) {
          (acc: List[ValidacionClave], r: Regla) =>
            r.validar(input, idUsuario, perfilUsuario, f.get(r.name))
              .map(_ :: acc)
              .getOrElse(acc)
        }
      )
    })
  }

  def aplicarReglasValor(input: String, idUsuario: Option[Int], perfilUsuario: PerfilesUsuario.perfilUsuario,
    validaciones: Regla*): Future[Validation[PersistenceException, List[(ValidacionClave, String)]]] = {
    obtenerReglasToMap.map(_.flatMap { f =>
      zSuccess(
        validaciones.foldLeft(Nil: List[(ValidacionClave, String)]) {
          (acc: List[(ValidacionClave, String)], r: Regla) =>
            r.validar(input, idUsuario, perfilUsuario, f.get(r.name))
              .map(x => (x, f.getOrElse(r.name, "")) :: acc)
              .getOrElse(acc)
        }
      )
    })
  }

  private def obtenerReglasToMap: Future[Validation[PersistenceException, Map[String, String]]] = {
    val reglasFuture = DataAccessAdapter.consultarReglasContrasenas()
    reglasFuture.map(_.flatMap(list => zSuccess(list.map(x => (x.llave, x.valor)).toMap)))
  }

  def reglasGenerales = List(MinCaracteres, MinCaracteresEspeciales, MinNumDigitos, MinMayusculas, MinMinusculas, CaracteresPermitidos, UltimasContrasenas)

  def reglasGeneralesAutoregistro = List(MinCaracteres, MinCaracteresEspeciales, MinNumDigitos, MinMayusculas, MinMinusculas, CaracteresPermitidos)

  //TODO: CambioContrasena Falta realizar la validacion de la fecha sumando los d??as establecidos en la DB
  def reglasIngresoUsuario = List(IntentosIngresoContrasena, CambioContrasena)

}