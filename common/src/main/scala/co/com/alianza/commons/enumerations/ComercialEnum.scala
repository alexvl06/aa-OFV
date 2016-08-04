package co.com.alianza.commons.enumerations

/**
 * Trait that represents a generic enumeration
 */
trait ComercialEnum extends Enumeration {

  /**
   * Method that check if an Integer value is contained in this enumeration
   * @param value Value to check
   * @return True if is contained, false otherwise
   */
  def contains( value: Int ): Boolean = values.exists( _.id == value )

  /**
   * Method that check if a String value is contained in this enumeration
   * @param value Value to check
   * @return True if is contained, false otherwise
   */
  def contains( value: String ): Boolean = values.exists( _.toString == value )

}
