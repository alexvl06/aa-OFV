package co.com.alianza.persistence.entities

import org.joda.time.DateTime

sealed trait GenericUser

/**
 * Class that represents and administrator in domain context
 * @param username Admins username
 * @param name Admins name
 * @param email Admins email
 * @param last_login_ip Admins last login ip
 * @param last_login_date Admins last login date
 */
case class Admin(
  username: String,
  name: String,
  email: String,
  last_login_ip: Option[String],
  last_login_date: Option[DateTime],
  recover_date: Option[DateTime]
) extends GenericUser

/**
 * Companion object for domain entity Admin
 */
object Admin {

  import play.api.libs.json._
  import play.api.libs.functional.syntax._

  implicit val adminWrites: Writes[Admin] = (
    (__ \ "username").write[String] and
    (__ \ "name").write[String] and
    (__ \ "email").write[String] and
    (__ \ "last_login_ip").write[Option[String]] and
    (__ \ "last_login_date").write[Option[DateTime]] and
    (__ \ "recover_date").write[Option[DateTime]]
  )(unlift(Admin.unapply))

  def apply(username: String, name: String, email: String): Admin = Admin(username, name, email, None, None, None)

}

/**
 * Class that represents an User in domain context
 * @param sAMAccountName Users username
 * @param sAMAccountType Users account type
 * @param distinguishedName User distinguished name
 * @param sn Users sn
 * @param givenName Users given name
 * @param memberOf Users member of
 * @param userPrincipalName Users principal name
 * @param last_login_ip User's last login ip
 * @param last_login_date user's last login date
 */
case class User(
  sAMAccountName: String,
  sAMAccountType: Option[String],
  distinguishedName: Option[String],
  sn: Option[String],
  givenName: Option[String],
  memberOf: Option[String],
  userPrincipalName: Option[String],
  idRole: Option[Int],
  last_login_ip: Option[String],
  last_login_date: Option[DateTime]
) extends GenericUser

/**
 * Companion object for domain entity User
 */
object User {

  import play.api.libs.json._
  import play.api.libs.functional.syntax._

  implicit val userWrites: Writes[User] = (
    (__ \ "sAMAccountName").write[String] and
    (__ \ "sAMAccountType").write[Option[String]] and
    (__ \ "distinguishedName").write[Option[String]] and
    (__ \ "sn").write[Option[String]] and
    (__ \ "givenName").write[Option[String]] and
    (__ \ "memberOf").write[Option[String]] and
    (__ \ "userPrincipalName").write[Option[String]] and
    (__ \ "idRole").write[Option[Int]] and
    (__ \ "last_login_ip").write[Option[String]] and
    (__ \ "last_login_date").write[Option[DateTime]]
  )(unlift(User.unapply))

  def apply(sAMAccountName: String): User = User(sAMAccountName, None, None, None, None, None, None, None, None, None)

}