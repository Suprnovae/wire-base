package test.models

import anorm._
import anorm.SqlParser._

import org.specs2.mutable._

import java.awt.Point
import java.util.{ Date, UUID }

import models._

import play.api.db.DB
import play.api.Play.current
import play.api.test._
import play.api.test.Helpers._

import scala.util.{ Random, Try }

class WithdrawalSpec extends Specification {
  step {
    running(FakeApplication()) {
      DB.withConnection { implicit c =>
        SQL("""DELETE FROM withdrawals""").executeUpdate()
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
  "Withdrawal" should {
    "be retrieved by id" in empty_set {
      running(FakeApplication()) {
        DB.withConnection { implicit connection =>
          def insert(transaction:Transaction, cash_point:CashPoint):List[UUID] = {
            SQL("""
              INSERT INTO withdrawals (
                cash_point_id,
                transaction_id
              ) values (
                {cash_point},
                {transaction}
              )"""
            ).on(
              "cash_point"  -> cash_point.id, 
              "transaction" -> transaction.id
            ).executeInsert(parser *)
          }

          val t = Transaction.create(
            2290,
            Receiver("Huey Freeman", "10921923", "US"),
            Sender("Riley Freeman", "1290192031", "US", "Atlanta", "Bourgeoisie Lane", None, "ella@example.com"),
            "boondocks"
          )
          val p = CashPoint.create(
            "US_NYC_MANH_WALLSTR_0012",
            Location(new Point(232, 433), "Wall Street 12", "New York City", "US"),
            Some("The ATM near the NYSE at 11 Wall Street")
          )
          println(t)
          println(p)
          val new_ids = insert(t.get, p.get)
          val new_id = new_ids.head
          println(new_ids)
          Withdrawal.findById(new_id).isDefined === true
        }
        ok
      }
    }
    "be retrievable by transaction id" in { pending }
    "fails when adding withdrawal without references" in { pending }
  }

  object empty_set extends Before {
    def before {
      running(FakeApplication()) {
        DB.withConnection { implicit c =>
          SQL("""DELETE FROM withdrawals""").executeUpdate()
          SQL("""DELETE FROM transactions""").executeUpdate()
          SQL("""DELETE FROM cash_points""").executeUpdate()
        }
      }
    }
  }
}
