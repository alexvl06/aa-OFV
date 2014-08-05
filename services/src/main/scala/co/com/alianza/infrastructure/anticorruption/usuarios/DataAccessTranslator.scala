package co.com.alianza.infrastructure.anticorruption.usuarios

import co.com.alianza.persistence.entities.{Usuario => dUsuario}
import co.com.alianza.infrastructure.dto.Usuario
import java.util.Date

/**
 *
 * @author seven4n
 */
object  DataAccessTranslator {

  def translateUsuario(usuario:List[dUsuario]) = {
   usuario map ( usu => Usuario(usu.id, usu.correo,new Date(usu.fechaActualizacion.getTime) , usu.identificacion, usu.tipoIdentificacion, usu.estado, usu.contrasena, usu.numeroIngresosErroneos, usu.ipUltimoIngreso, usu.fechaUltimoIngreso  ))
  }

  def translateUsuario(usu:dUsuario) = {
    Usuario(usu.id, usu.correo,new Date(usu.fechaActualizacion.getTime) , usu.identificacion, usu.tipoIdentificacion, usu.estado, usu.contrasena, usu.numeroIngresosErroneos, usu.ipUltimoIngreso, usu.fechaUltimoIngreso  )
  }

}
