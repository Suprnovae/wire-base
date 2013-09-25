package test.models

import anorm._
import anorm.SqlParser._

import org.specs2.mutable._

import java.awt.Point
import java.util.{ UUID }

import models._

import play.api.db.DB
import play.api.Play.current
import play.api.test._
import play.api.test.Helpers._

import scala.util.{ Random, Try }

class UserSpec extends Specification {
  step {
    running(FakeApplication()) {
      DB.withConnection { implicit connection => 
        SQL("""DELETE FROM users""").executeUpdate()
      }
    }
  }
  implicit def rowToUUID: Column[UUID] = {
    Column.nonNull[UUID] { (value, meta) =>
      value match {
        case uuid: UUID     => Right(uuid)
        case string: String => Right(UUID.fromString(string))
      }
    }
  }
  val parser = { get[UUID]("id") }
  "User" should {
    "be retrievable by id" in { 
      running(FakeApplication()) {
        DB.withConnection { implicit connection =>
          def insert(handle: String, secret: String): List[UUID] = {
            SQL("""INSERT INTO users 
            (handle, secret)
            values (
              {handle},
              {secret}
            )""").on(
              'handle -> handle,
              'secret -> secret
            ).executeInsert(parser *)
          }
          val new_id = insert("joker", "why so serious?").head
          User.findById(new_id).isDefined === true
          User.findById(new_id).foreach(user =>
            user.handle === "joker"
          )
        }
      }
      ok
    }
    "be retrievable by handle" in empty_set {
      running(FakeApplication()) {
        User.findByHandle("jesus").isEmpty === true
        User.findByHandle("jesus") === None
        User.create("jesus", "Yes I did!")
        User.findByHandle("jesus").isEmpty === false
      }
    }
    "be nothing upon fetching a non-existent UUID" in { 
      running(FakeApplication()) {
        User.findById(new UUID(0, 0)) === None
      }
    }
    "be instantiable" in {
      val user = new User (
        UUID.randomUUID,
        "kwame",
        "UNKNOWN",
        "some shit"
      )
      user.handle === "kwame"
      user.status === "UNKNOWN"
      user.secretHash === "some shit"
    }
    "be creatable with helper" in { 
      running(FakeApplication()) {
        val p = User.create(
          "cartman",
          "screw you guys"
        )
        p.isSuccess === true
        p.get.handle === "cartman"
      }
    }
    "fail when creating user with already existing handle" in {
      running(FakeApplication()) {
        User.create("naveen", "was lost!").isSuccess === true;
        User.create("naveen", "still lost...").isSuccess === false;

        User.create("vincent", "walt's dog!").isFailure === false;
        User.create("vincent", "woof woof!!!").isFailure === true;
      }
    }
    "return empty list when there are no users" in empty_set {
      running(FakeApplication()) {
        User.findAll.isEmpty === true
      }
    }
    // TODO: Test validation when token field in db contains a non bcrypt phrase, this currently throws an IllegalArgumentException
    "authenticate user by credentials" in {
      running(FakeApplication()) {
        val p = User.create("Agent123", "password")
        p.isSuccess === true
        p.get.handle === "Agent123"
        User.validate("Agent123", "password1") === false
        User.validate("Agent12", "password") === false
        User.validate("Agent123 ", "password") === false
        User.validate("Agent123", "password") === true
      }
    }
    "authenticate hard-coded credentials if no other user exist" in empty_set {
      running(FakeApplication()) {
        User.validate("wire", "wow!") === true
        User.create("jack", "sparrow").isSuccess === true
        User.validate("wire", "wow!") === true
      }
    }
    "respond to type helpers" in empty_set {
      running(FakeApplication()) {
        val user = User.create("sawyer", "Hey there, freckles!")
        user.isSuccess === true
        user.get.isAdmin === false
        user.get.isClerk === false
        user.get.isClient === false
      }
    }
    "have a cashpoint if an CashPoint clerk" in { todo }
  }

  object empty_set extends Before {
    def before {
      running(FakeApplication()) {
        DB.withConnection { implicit c =>
          SQL("""DELETE FROM users""").executeUpdate()
        }
      }
    }
  }
}
