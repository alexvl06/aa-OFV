package portal.transaccional.autenticacion.service.drivers.reglas

import java.util.regex.Pattern

import co.com.alianza.exceptions.ValidacionException
import co.com.alianza.persistence.entities.{ ReglaContrasena, UltimaContrasena }
import co.com.alianza.util.clave.Crypto
import enumerations.{ AppendPasswordUser, PerfilesUsuario }
import portal.transaccional.autenticacion.service.drivers.ultimaContrasena.UltimaContrasenaRepository
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.ReglaContrasenaDAOs

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by hernando on 25/07/16.
 */
case class ReglaContrasenaDriverRepository(
    reglaDAO: ReglaContrasenaDAOs,
    ultimaContrasenaRepo: UltimaContrasenaRepository
)(implicit val ex: ExecutionContext) extends ReglaContrasenaRepository {

  val REGLAS_GENERALES = List(MinCaracteres, MinCaracteresEspeciales, MinNumDigitos, MinMayusculas, MinMinusculas, CaracteresPermitidos, UltimasContrasenas)

  val REGLAS_AUTOREGISTRO = List(MinCaracteres, MinCaracteresEspeciales, MinNumDigitos, MinMayusculas, MinMinusculas, CaracteresPermitidos)

  //TODO: CambioContrasena Falta realizar la validacion de la fecha sumando los días establecidos en la DB
  val REGLAS_INGRESO_USUARIO = List(IntentosIngresoContrasena, CambioContrasena)

  def getRegla(llave: String): Future[ReglaContrasena] = {
    reglaDAO.getByKey(llave)
  }

  def getReglas(): Future[Seq[ReglaContrasena]] = {
    reglaDAO.getAll()
  }

  def validarContrasenaReglasGenerales(idUsuario: Int, perfilUsuario: PerfilesUsuario.perfilUsuario, contrasena: String): Future[Boolean] = {
    def obtenerUltimasContrasenas(cantidad: Int): Future[Seq[UltimaContrasena]] = perfilUsuario match {
      case PerfilesUsuario.clienteIndividual => ultimaContrasenaRepo.getUltimasContrasenas(idUsuario, cantidad)
      case PerfilesUsuario.clienteAdministrador => ultimaContrasenaRepo.getUltimasContrasenasAdmin(idUsuario, cantidad)
      case PerfilesUsuario.agenteEmpresarial => ultimaContrasenaRepo.getUltimasContrasenasAgente(idUsuario, cantidad)
      case _ => Future.failed(new Exception(ErrorUltimasContrasenas.toString))
    }
    for {
      reglas <- getReglas()
      mapa <- Future.successful(reglas.map(x => (x.llave, x.valor)).toMap)
      ultimasContrasenas <- obtenerUltimasContrasenas(mapa.get("ULTIMAS_CONTRASENAS_NO_VALIDAS").getOrElse("0")toInt)
      validacion <- aplicarReglas(contrasena, mapa, Option(ultimasContrasenas), REGLAS_GENERALES)
    } yield validacion
  }

  def validarContrasenaReglasAutorregistro(contrasena: String): Future[Boolean] = {
    for {
      reglas <- getReglas()
      mapa <- Future.successful(reglas.map(x => (x.llave, x.valor)).toMap)
      validacion <- aplicarReglas(contrasena, mapa, None, REGLAS_AUTOREGISTRO)
    } yield validacion
  }

  def validarContrasenaReglasIngresoUsuario(contrasena: String): Future[Boolean] = {
    for {
      reglas <- getReglas()
      mapa <- Future.successful(reglas.map(x => (x.llave, x.valor)).toMap)
      validacion <- aplicarReglas(contrasena, mapa, None, REGLAS_INGRESO_USUARIO)
    } yield validacion
  }

  def aplicarReglas(contrasena: String, mapa: Map[String, String], ultimasContrasenas: Option[Seq[UltimaContrasena]], reglas: List[Regla]): Future[Boolean] = {
    val optionlistvalidacion: List[Option[ValidacionClave]] = reglas.map {
      regla => regla.validar(contrasena, mapa.get(regla.name), ultimasContrasenas)
    }
    val listaErrores: List[ValidacionClave] = optionlistvalidacion.filter(_.isDefined).map(_.get)
    listaErrores.isEmpty match {
      case true => Future.successful(true)
      case _ => Future.failed(ValidacionException("409.12", listaErrores.mkString("-")))
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  /////////////////////VALIDACIONES INDIVIDUALES /////////////////////////////
  /////////////////////POR CADA UNA DE LAS REGLAS/////////////////////////////
  ////////////////////////////////////////////////////////////////////////////

  case object MinCaracteres extends Regla("MINIMO_NUMERO_CARACTERES") {
    def validar(valor: String, condicion: Option[String], ultimasContrasenas: Option[Seq[UltimaContrasena]]): Option[ValidacionClave] = {
      condicion match {
        case Some(value) => if (valor.length < value.toInt) Some(ErrorMinCaracteres) else None
        case None => Some(ErrorMinCaracteres)
      }
    }
  }

  case object MinCaracteresEspeciales extends Regla("MINIMO_NUMERO_CARACTERES_ESPECIALES") {
    def validar(valor: String, condicion: Option[String], ultimasContrasenas: Option[Seq[UltimaContrasena]]): Option[ValidacionClave] = {
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
    def validar(valor: String, condicion: Option[String], ultimasContrasenas: Option[Seq[UltimaContrasena]]): Option[ValidacionClave] = {
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
    def validar(valor: String, condicion: Option[String], ultimasContrasenas: Option[Seq[UltimaContrasena]]): Option[ValidacionClave] = {
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
    def validar(valor: String, condicion: Option[String], ultimasContrasenas: Option[Seq[UltimaContrasena]]): Option[ValidacionClave] = {
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
    def validar(valor: String, condicion: Option[String], ultimasContrasenas: Option[Seq[UltimaContrasena]]): Option[ValidacionClave] = {
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
    def validar(valor: String, condicion: Option[String], ultimasContrasenas: Option[Seq[UltimaContrasena]]): Option[ValidacionClave] = {
      condicion match {
        case Some(value) => if (valor.toInt == value.toInt) Some(ErrorIntentosErroneos) else None
        case None => Some(ErrorIntentosErroneos)
      }
    }
  }

  case object CambioContrasena extends Regla("DIAS_VALIDA") {
    def validar(valor: String, condicion: Option[String], ultimasContrasenas: Option[Seq[UltimaContrasena]]): Option[ValidacionClave] = {
      condicion match {
        case Some(value) => if (valor.toInt == value.toInt) Some(ErrorIntentosErroneos) else None
        case None => Some(ErrorIntentosErroneos)
      }
    }
  }

  case object UltimasContrasenas extends Regla("ULTIMAS_CONTRASENAS_NO_VALIDAS") {
    def validar(contrasena: String, condicion: Option[String], ultimasContrasenas: Option[Seq[UltimaContrasena]]): Option[ValidacionClave] = {
      condicion match {
        case Some(value) =>
          val cantidad: Int = value.toInt
          ultimasContrasenas match {
            case Some(contrasenas) if (contrasenas.isEmpty) => None
            case Some(contrasenas) if (contrasenas.size <= cantidad) =>
              val contrasenaHash: String = Crypto.hashSha512(contrasena.concat(AppendPasswordUser.appendUsuariosFiducia), contrasenas.head.idUsuario)
              if (contrasenas.exists(_.contrasena.equals(contrasenaHash))) Some(ErrorUltimasContrasenas)
              else None
            case _ => Some(ErrorUltimasContrasenas)
          }
        case None => Some(ErrorUltimasContrasenas)
      }
    }
  }

  abstract sealed class Regla(val name: String) {
    def validar(valor: String, condicion: Option[String], ultimasContrasenas: Option[Seq[UltimaContrasena]]): Option[ValidacionClave]
  }

}
