package models

import anorm._
import anorm.SqlParser._
import com.github.t3hnar.bcrypt._
import java.util.{ Date, UUID }
import play.api.db.DB
import play.api.Play.current
import scala.util.Try;

case class User(
  id: UUID,
  handle: String,
  status: String,
  secretHash: String
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
        status,
        secret
      )
    }
  }

  def create(handle:String, password:String): Try[User] = {
    Try(
        DB.withConnection { implicit connection =>
          val res = SQL("""
            INSERT INTO users (
              handle,
              secret
            ) values (
              {handle},
              {secret}
            )"""
          ).on(
            'handle -> handle,
            'secret -> (handle+password).bcrypt
          ).executeInsert[List[User]](User.simple *)
          println("result is + " + res.head.toString)
          User.findById(res.head.id).get
        }
    )
  }

  def findAll: Seq[User] = {
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM users").as(User.simple *)
    }
  }
  def findById(id: UUID): Option[User] = {
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM users WHERE id = {id}").on(
        'id -> id
      ).as(User.simple.singleOpt)
    }
  }
  def findByHandle(handle: String): Option[User] = {
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM users WHERE handle = {handle}").on(
        'handle -> handle
      ).as(User.simple.singleOpt)
    }
  }

  def validate(id: UUID, handle: String, password: String): Boolean = {
    User.findById(id) match {
      case Some(t) => if ((handle+password).isBcrypted(t.secretHash)) true else false
      case None => false
      case _ => false
    }
  }
}
