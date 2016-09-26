package co.com.alianza.util.json

import spray.http.Uri

trait HalPaginationUtils {

  def getHalLinks(totalItems: Int, itemsPerPage: Int, pageNumber: Int,
                  relativeUri: Uri, queryParamsMap: Map[String, String]): Map[String, String] = {
    if (totalItems == 0) {
      Map("self" -> s"$relativeUri")
    } else {
      val firstPage: Int = 1
      val lastPage: Int = getLastPage(totalItems, itemsPerPage)
      val previousPage: Option[Int] = getPreviousPage(pageNumber, lastPage)
      val nextPage: Option[Int] = getNextPage(pageNumber, lastPage)
      Map(
        "self" -> s"$relativeUri",
        buildPageLink(firstPage, "first", relativeUri, queryParamsMap),
        buildPageLink(lastPage, "last", relativeUri, queryParamsMap)
      ) ++
        buildPageLink(previousPage, "previous", relativeUri, queryParamsMap) ++
        buildPageLink(nextPage, "next", relativeUri, queryParamsMap)
    }
  }

  private def getLastPage(totalItems: Int, itemsPerPage: Int): Int = {
    itemsPerPage match {
      case x if x > 0 => math.ceil(totalItems.toFloat / itemsPerPage).toInt
      case _ => 1
    }
  }

  private def getPreviousPage(pageNumber: Int, lastPage: Int): Option[Int] = {
    if (pageNumber > 1 && pageNumber <= lastPage) {
      Some(pageNumber - 1)
    } else {
      None
    }
  }

  private def getNextPage(pageNumber: Int, lastPage: Int): Option[Int] = {
    if (pageNumber >= 1 && pageNumber < lastPage) {
      Some(pageNumber + 1)
    } else {
      None
    }
  }

  private def buildPageLink(page: Int, pageName: String,
                            relativeUri: Uri, queryParamsMap: Map[String, String]): (String, String) = {
    pageName -> s"${relativeUri.withQuery(queryParamsMap + ("pagina" -> s"$page"))}"
  }

  private def buildPageLink(page: Option[Int], pageName: String,
                            relativeUri: Uri, queryParamsMap: Map[String, String]): Seq[(String, String)] = {
    page.map(p => {
      (pageName -> s"${relativeUri.withQuery(queryParamsMap + ("pagina" -> s"$p"))}") :: Nil
    }).getOrElse(Nil)
  }
}
