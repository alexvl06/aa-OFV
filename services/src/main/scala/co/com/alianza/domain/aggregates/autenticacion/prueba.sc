import co.com.alianza.infrastructure.dto.RecursoUsuario
/**
 *
 * @author smontanez
 */

object Validar {

  def validarRecursos(recursos:List[RecursoUsuario], url:String)  = {

    recursos.filter(x => filtrarUrl(x, url))

  }

  def filtrarUrl (recurso:RecursoUsuario, url:String):Boolean = {
    if(recurso.urlRecurso.equals(url)){
      //ResponseMessage(OK)
      true
    }else if(recurso.urlRecurso.endsWith("/*")) {

      if(url.length > recurso.urlRecurso.lastIndexOf("/*")) {
        val urlSuffix = url.substring(0,recurso.urlRecurso.lastIndexOf("/*")+1 )
        val urlC = recurso.urlRecurso.substring(0,recurso.urlRecurso.lastIndexOf("/*"))
        if(urlSuffix.equals(urlC)){
          recurso.acceso
        }else
          false
      }else false
    }else{
      //ResponseMessage(Unauthorized, "No tiene permisos para acceder al recurso solicitado")
      false
    }
  }
}
val recursos = RecursoUsuario(12, "/consultas/fondos/*", true, None) :: Nil




val aa = Validar.validarRecursos(recursos, "/consultas/fondos")




println(aa)






