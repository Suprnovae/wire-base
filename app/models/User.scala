package models

import anorm._
import anorm.SqlParser._
import java.util.{ Date, UUID }

case class User(
  id: UUID,
  handle: String,
  status: String
)

object User extends Model {
  val simple = {
    get[UUID]("users.id")~
    get[String]("users.handle")~
    get[String]("users.secret")~
    get[String]("users.status")~
    get[Date]("users.created_at") map {
      case id~handle~secret~status~created_at =>
      User(
        id,
        handle,
        status
      )
    }
  }

  def create(handle:String, password:String): Option[User] = {
    None
  }

  def findAll: Seq[User] = List[User]()
  def findById(id: UUID): Option[User] = {
    None
  }

  def validate(handle: String, password: String): Boolean = {
    false
  }
}
