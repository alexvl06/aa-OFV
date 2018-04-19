package co.com.alianza.persistence.entities

import java.sql.Timestamp

import scala.reflect._
import CustomDriver.simple._
/**
 * Created by alexandra in 2016
 */
trait UsuarioAgente {
  val id: Int
  val identificacion: String
  val usuario: String
  val correo: String
  val estado: Int
  val contrasena: Option[String]
  val token: Option[String]
  val fechaActualizacion: Timestamp
  val numeroIngresosErroneos: Int
  val ipUltimoIngreso: Option[String]
  val fechaUltimoIngreso: Option[Timestamp]
  val tipoIdentificacion: Int
  val descripcion: Option[String]
  var interventor: Boolean = false
}

abstract class UsuarioAgenteTable[E: ClassTag](tag: Tag, tableName: String)
    extends Table[E](tag, tableName) {

  val classOfEntity = classTag[E].runtimeClass

  val id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  val identificacion = column[String]("NIT_EMPRESA")
  val tipoIdentificacion = column[Int]("TIPO_IDENTIFICACION")
  val usuario = column[String]("USUARIO")
  val correo = column[String]("CORREO")
  val estado = column[Int]("ESTADO")
  val contrasena = column[Option[String]]("CONTRASENA")
  val token = column[Option[String]]("TOKEN")
  val fechaActualizacion = column[Timestamp]("FECHA_ACTUALIZACION")
  val numeroIngresosErroneos = column[Int]("NUMERO_INGRESOS_ERRONEOS")
  val ipUltimoIngreso = column[Option[String]]("IP_ULTIMO_INGRESO")
  val descripcion = column[Option[String]]("DESCRIPCION")
  val fechaUltimoIngreso = column[Option[Timestamp]]("FECHA_ULTIMO_INGRESO")
}

