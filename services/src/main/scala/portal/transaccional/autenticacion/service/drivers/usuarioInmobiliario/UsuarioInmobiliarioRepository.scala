package portal.transaccional.autenticacion.service.drivers.usuarioInmobiliario

import scala.concurrent.Future

/**
  * Define las operaciones del repositorio de agentes inmobiliarios
  */
trait UsuarioInmobiliarioRepository {

  /**
    * Crea un agente inmobiliario
    *
    * @param tipoIdentificacion Tipo de identificación de la empresa a agregar el agente
    * @param identificacion     Número de identificación de la empresa a agregar el agente
    * @param correo             Correo electrónico del agente
    * @param usuario            Nombre de usuario del agente
    * @param nombre             Nombre completo del agente - Opcional
    * @param cargo              Cargo del agente en la empresa - Opcional
    * @param descripcion        Descripción del agente - Opcional
    * @return Un futuro con el identificador del agente agregado
    */
  def createAgenteInmobiliario(tipoIdentificacion: Int, identificacion: String,
                               correo: String, usuario: String,
                               nombre: Option[String], cargo: Option[String], descripcion: Option[String]): Future[Int]
}
