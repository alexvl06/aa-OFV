package portal.transaccional.autenticacion.service.drivers.menu

import co.com.alianza.exceptions.NoAutorizado
import co.com.alianza.persistence.entities.Menu
import co.com.alianza.util.token.{ AesUtil, Token }
import portal.transaccional.autenticacion.service.drivers.sesion.SesionRepository
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.AlianzaDAO
import spray.json.{ JsArray, JsValue, JsonFormat, RootJsonFormat, deserializationError }

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }
import spray.json._

case class MenuResponse(menuUsuario: List[MenuUsuario])
case class MenuUsuario(menu: String, item: ListBuffer[ItemMenu])
case class ItemMenu(idItem: Int, titulo: String, posicion: Int, url: Option[String], subMenu: ListBuffer[ItemMenuSub])
case class ItemMenuSub(idItem: Int, titulo: String, posicion: Int, url: Option[String], subMenu: ListBuffer[SubItemMenu]) {
  implicit def listBufferFormat[T: JsonFormat] = new RootJsonFormat[ListBuffer[T]] {
    def write(listBuffer: ListBuffer[T]) = JsArray(listBuffer.map(_.toJson).toVector)
    def read(value: JsValue): ListBuffer[T] = value match {
      case JsArray(elements) => elements.map(_.convertTo[T])(collection.breakOut)
      case x => deserializationError("Expected ListBuffer as JsArray, but got " + x)
    }
  }
}
case class SubItemMenu(idItem: Int, titulo: String, posicion: Int, url: Option[String])

case class MenuUsuarioDriverRepository(
    alianzaDAO: AlianzaDAO,
    sesionRepo: SesionRepository
)(implicit val ex: ExecutionContext) extends MenuUsuarioRepository {

  /**
   * Obtiene el menú para un perfil determinado
   * @return Future[MenuResponse]
   */
  def getMenu(tokenEncripted: String): Future[MenuResponse] = {
    var token = AesUtil.desencriptarToken(tokenEncripted)
    var funMenu = getMenuByPerfil(token)
    for {
      validatoken <- validarToken(token)
      validarSesion <- sesionRepo.validarSesion(token)
      menuDataBD <- getMenuByPerfil(token)
      menuUser <- constructMenu(menuDataBD)
    } yield menuUser
  }

  private def constructMenu(menuOpt: Seq[(Menu, Int, String)]): Future[MenuResponse] = {
    var listResult = menuOpt.to[ListBuffer]
    val modules = scala.collection.mutable.Map[String, ListBuffer[ItemMenu]]()
    var sizeInicial = listResult.size + 1
    while (listResult.size > 0 && sizeInicial >= 0) {
      var posicion = 0
      while (posicion < listResult.size) {
        var seAgrego = false
        val (menu, permiso, moduloDesc) = listResult(posicion)

        if (!modules.contains(moduloDesc)) {
          modules.put(moduloDesc, new ListBuffer[ItemMenu])
        }

        if (menu.menuPadre.equals(None)) {
          modules.get(moduloDesc).get += ItemMenu(menu.idMenu, menu.titulo, menu.posicion, menu.url, new ListBuffer[ItemMenuSub])
          seAgrego = true
        } else if (menu.menuPadre.get > 0) {
          var listaMenu = modules.get(moduloDesc).get
          for (j <- 0 to listaMenu.size - 1) {
            if (listaMenu(j).idItem == menu.menuPadre.get) {
              listaMenu(j).subMenu += ItemMenuSub(menu.idMenu, menu.titulo, menu.posicion, menu.url, new ListBuffer[SubItemMenu])
              seAgrego = true
            } else if (listaMenu(j).subMenu.size > 0 && permiso != null) {
              for (subMenu <- listaMenu(j).subMenu) {
                if (subMenu.idItem == menu.menuPadre.get) {
                  subMenu.subMenu += SubItemMenu(menu.idMenu, menu.titulo, menu.posicion, menu.url)
                  seAgrego = true
                }
              }
            }
          }
        }
        if (seAgrego) {
          listResult.remove(posicion)
        } else {
          posicion = posicion + 1
        }
      }
      sizeInicial = sizeInicial - 1
    }
    var listaRta = new ListBuffer[MenuUsuario]
    modules.foreach(e => listaRta += MenuUsuario(e._1, e._2))
    var menuResponse = MenuResponse(listaRta.toList)
    Future.successful(menuResponse)
  }

  private def getMenuByPerfil(token: String): Future[Seq[(Menu, Int, String)]] = {
    var idPerfil = getTokenData(token, "tipoCliente").toInt
    for {
      menu <- alianzaDAO.getMenuByPerfil(idPerfil)
    } yield menu
  }

  private def getTokenData(token: String, keyData: String): String = {
    val nToken = Token.getToken(token).getJWTClaimsSet
    nToken.getCustomClaim(keyData).toString
  }

  private def validarToken(token: String): Future[Boolean] = {
    Token.autorizarToken(token) match {
      case true => Future.successful(true)
      case _ => Future.failed(NoAutorizado("Token erróneo"))
    }
  }
}
