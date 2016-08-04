package co.com.alianza.commons.enumerations

object UserTypesEnumeration extends ComercialEnum {

  /**
   * Value for user type ADMIN
   */
  val admin = Value(0, "ADMIN")

  /**
   * Value for user type FIDUCIARIA
   */
  val fiduciaria = Value(1, "FID")

  /**
   * Value for user type VALORES
   */
  val valores = Value(2, "VAL")

}
