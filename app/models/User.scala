package models

import anorm._
import anorm.SqlParser._
import com.github.t3hnar.bcrypt._
import java.util.{ Date, UUID }
import play.api.db.DB
import play.api.Play.current
import scala.util.Try;

case class UserForm(
  handle: String,
  secret: String,
  secret_repeat: String
)

class User(
  uuid: UUID,
  user_handle: String,
  user_status: String,
  hash: String,
  role: Int = 0
) {
  def id = uuid
  def handle = user_handle
  def secretHash = hash
  def status = user_status 

  def isSuspended: Boolean = { false }

  def isAdmin: Boolean =  { if (role == 2) true else false }
  def isClerk: Boolean =  { if (role == 1) true else false }
  def isClient: Boolean = { if (role == 0) true else false }
}

object User extends Model {
  val simple = {
    get[UUID]("users.id")~
    get[String]("users.handle")~
    get[String]("users.secret")~
    get[String]("users.status")~
    get[Int]("users.class")~
    get[Date]("users.created_at") map {
      case id~handle~secret~status~role~created_at =>
      new User(
        id,
        handle,
        status,
        secret,
        role
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
        User.findById(res.head.id).get
      }
    )
  }

  def count: Long = {
    DB.withConnection { implicit c =>
      SQL("""SELECT COUNT(*) AS c FROM users""")
        .apply().head[Long]("c")
    }
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

  def validate(handle: String, password: String): Boolean = {
    User.findByHandle(handle) match {
      case Some(t) => if ((handle+password).isBcrypted(t.secretHash)) true else false
      // default credentials
      case None => if(handle == "wire" && password == "wow!") true else false
      case _ => false
    }
  }
}
