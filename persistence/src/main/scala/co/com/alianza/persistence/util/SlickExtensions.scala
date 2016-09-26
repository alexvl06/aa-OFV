package co.com.alianza.persistence.util

import slick.lifted.{CanBeQueryCondition, Query, Rep}

/**
  * Trait that defines extensions for TableQuery
  */
trait SlickExtensions {

  protected val defaultPage: Int = 1
  protected val defaultPageSize: Int = 200

  /**
    * Optionally filter on a column with a supplied predicate
    *
    * @param query The initial query on which the filters must be applied
    */
  case class MaybeFilter[E, U, C[_]](query: Query[E, U, C]) {

    /**
      * Filter with predicate `pred` only if `data` is defined
      */
    def filter[X, T <: Rep[_]](data: Option[X])(pred: X => E => T)(implicit wr: CanBeQueryCondition[T]): MaybeFilter[E, U, C] = {
      data match {
        case Some(value) => MaybeFilter(query.filter(pred(value)))
        case None => this
      }
    }

  }

  /**
    * Implicit class that defines extensions for generic Query types
    */
  implicit class QueryExtensions[E, U, C[_]](val q: Query[E, U, C]) {
    /**
      * Method that creates a paginated query
      *
      * @param pageNumberOp Page number option
      * @param pageSizeOp   Page size option
      * @return Created paginated query if pageNumberOp and pageSizeOp are defined.
      *         It will return page 1 with defaultSize(200) query otherwise.
      */
    def paginate(pageNumberOp: Option[Int], pageSizeOp: Option[Int]): Query[E, U, C] = {
      val (pageNumber: Int, pageSize: Int) = (pageNumberOp.getOrElse(defaultPage), pageSizeOp.getOrElse(defaultPageSize))
      q.drop((pageNumber - 1) * pageSize).take(pageSize)
    }

  }

}