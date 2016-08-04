package co.com.alianza.persistence.util.mappers

import java.sql.Timestamp

import org.joda.time.DateTime
import slick.driver.JdbcProfile

object JodatimeMapper {

  /**
   * Implicit JodaTime mapper for slick
   * @param profile Jdbc driver
   * @return JodaTime mapper
   */
  implicit def mapper()(implicit profile: JdbcProfile): profile.BaseColumnType[DateTime] = {
    import profile.api._
    MappedColumnType.base[DateTime, Timestamp](
      dt => new Timestamp(dt.getMillis),
      ts => new DateTime(ts.getTime)
    )
  }

}
