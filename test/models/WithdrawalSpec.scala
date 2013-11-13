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
    "be retrievable by id" in empty_set {
      running(FakeApplication()) {
        DB.withConnection { implicit connection =>
          def insertWithdrawal(transaction:Transaction, cash_point:CashPoint):List[UUID] = {
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
          t.isDefined === true
          val p = CashPoint.create(
            "US_NYC_MANH_WALLSTR_0012",
            Location(new Point(232, 433), "Wall Street 12", "New York City", "US"),
            Some("The ATM near the NYSE at 11 Wall Street")
          )
          p.isDefined === true
          val new_ids = insertWithdrawal(t.get, p.get)
          val new_id = new_ids.head

          Withdrawal.findById(new_id).isDefined === true
        }
        ok
      }
    }
    "should be listable by cash point" in empty_set {
      running(FakeApplication()) {
        DB.withConnection { implicit connection =>
          def insertWithdrawal(transaction:Transaction, cash_point:CashPoint):List[UUID] = {
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
          
          val p = CashPoint.create(
            "US_NYC_MANH_WALLSTR_0012",
            Location(new Point(232, 433), "Wall Street 12", "New York City", "US"),
            Some("The ATM near the NYSE at 11 Wall Street")
          )
          insertWithdrawal(Transaction.create(
            2290,
            Receiver("Bob Jones", "1789213828", "US"),
            Sender("Mina Tikra", "1290192031", "US", "Miami", "Sunway Strip", None, "mina@example.com"),
            "sunshine"
          ).get, p.get)
          insertWithdrawal(Transaction.create(
            1400,
            Receiver("James Earl", "0918235819293", "US"),
            Sender("Sandra Pine", "19230188832", "US", "Palo Alto", "Infinity Lane", None, "sandra@example.com"),
            "Pinewood Derby"
          ).get, p.get)
          Withdrawal.findByCashPoint(p.get).length === 2
        }
        ok
      }
    }
    "be retrievable by transaction id" in { pending }
    "succeed on valid transaction and cashpoint" in empty_set {
      running(FakeApplication()) {
        val t = Transaction.create(
          400,
          Receiver("Christopher Wallace", "1238293842", "US"),
          Sender("Ella Fitzgerald", "2838384848", "US", "Atlanta", "Bourgeoisie Lane", None, "ella@example.com"),
          "boondocks"
        )
        t.isDefined === true
        val id = t.get.id
        Transaction.findById(id).get.withdrawal.isDefined === false
        val p = CashPoint.create(
          "US_NYC_MANH_WALLSTR_0012",
          Location(new Point(232, 433), "Wall Street 12", "New York City", "US"),
          Some("The ATM near the NYSE at 11 Wall Street")
        )
        p.isDefined === true
        CashPoint.modify(p.get.id, true).get.active === true

        val wt = Withdrawal.create(t.get, p.get)
        wt.isDefined === true
        val result = Transaction.findById(t.get.id).get
        result.withdrawal.get.date.after(result.deposit.date) === true
      }
    }
    "fail with invalid transaction" in empty_set {
      running(FakeApplication()) {
        val p = CashPoint.create(
          "US_NYC_MANH_WALLSTR_0012",
          Location(new Point(232, 433), "Wall Street 12", "New York City", "US"),
          Some("The ATM near the NYSE at 11 Wall Street")
        )
        val t = Transaction (
          UUID.randomUUID,
          2000,
          Receiver("Elvis Presley", "1238293842", "US"),
          Sender("Frank Sinatra", "283877821219", "US", "New York", "1st St 123", None, "ratpack@example.com"),
          Deposit(new Date(1230L)),
          None,
          "secrets kill",
          "obama"
        )
        p.isDefined === true
        Try(Withdrawal.create(t, p.get)).isFailure === true // must throwA[NoSuchElementException]
      }
    }
    "fail on already withdrawn transaction" in empty_set {
      running(FakeApplication()) {
        val t = Transaction.create(
          2290,
          Receiver("Huey Freeman", "10921923", "US"),
          Sender("Riley Freeman", "1290192031", "US", "Atlanta", "Bourgeoisie Lane", None, "ella@example.com"),
          "boondocks"
        )
        t.isDefined === true
        val p = CashPoint.create(
          "US_NYC_MANH_WALLSTR_0012",
          Location(new Point(232, 433), "Wall Street 12", "New York City", "US"),
          Some("The ATM near the NYSE at 11 Wall Street")
        )
        p.isDefined === true
        val wd = Withdrawal.create(t.get, p.get)
        wd.isDefined === true
        val date = wd.get.date
        Try(Withdrawal.create(t.get, p.get)).isFailure === true //.get must throwA[AlreadyWithdrawnException]
        Transaction.findById(t.get.id).get.withdrawal.get.date === date
      }
    }
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
