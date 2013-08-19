package test.models

import anorm._
import anorm.SqlParser._

import org.specs2.mutable._

import java.util.{ Date, UUID }

import models._

import play.api.db.DB
import play.api.Play.current
import play.api.test._
import play.api.test.Helpers._

import scala.util.{ Random, Try }

class TransactionSpec extends Specification {
  step {
    running(FakeApplication()) {
      DB.withConnection { implicit c =>
        SQL("""DELETE FROM transactions""").executeUpdate()
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
  "Transaction" should {
    "be retrieved by id" in {
      running(FakeApplication()) {
        DB.withConnection { implicit connection =>
          def insert(amount:Int, receiver:String, sender:String):List[UUID] = {
            SQL("""
              INSERT INTO TRANSACTIONS (
                amount,
                receiver_name,
                receiver_phonenumber,
                receiver_country,
                sender_name,
                sender_address,
                sender_phonenumber,
                sender_email,
                sender_city,
                sender_country,
                token,
                code 
              ) values (
                {amount},
                {receiver},
                '+5978765432123456',
                'SR',
                {sender},
                'Karel Doormanstraat 41091',
                '+31765437890984',
                'sender@example.com',
                'Teststad',
                'NL',
                'Awwwh',
                'Snap!'
              )"""
            ).on(
              "amount" -> amount, 
              "receiver" -> receiver, 
              "sender" -> sender
            ).executeInsert(parser *)
          }

          val new_ids = insert(420, "Eline Weson", "Orgeon Waterberg")
          val new_id = new_ids.head
          Transaction.findById(new_id).isDefined === true
          Transaction.findById(new_id).foreach(transaction => 
            transaction.amount === 420
          )
        }
        ok
      }
    }
    "be None upon fetching a non-existent UUID" in {
      running(FakeApplication()) {
        Transaction.findById(new UUID(0, 0)) === None
      }
    }
    "be instantiable" in {
      val transaction = Transaction (
        UUID.randomUUID,
        2000,
        Receiver("Elvis Presley", "1238293842", "US"),
        Sender("Frank Sinatra", "283877821219", "US", "New York", "1st St 123", None, "ratpack@example.com"),
        Deposit(new Date(1230L)),
        None,
        "secrets kill",
        "obama"
      )
      transaction.receiver.name === "Elvis Presley"
      transaction.sender.name === "Frank Sinatra"
      transaction.amount === BigDecimal.int2bigDecimal(2000)
    }
    "be creatable with helper" in {
      running(FakeApplication()) {
        val t = Transaction.create(
          400,
          Receiver("Elvis Presley", "1238293842", "US"),
          Sender("Frank Sinatra", "283877821219", "US", "New York", "1st St 123", None, "ratpack@example.com"),
          "secrets kill"
        )
        t.isDefined === true
        t.get.amount === BigDecimal.int2bigDecimal(400)
        t.get.receiver.country === "US"
      }
    }
    "be withdrawable" in {
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
        val wt = Transaction.withdraw(id)
        wt.withdrawal.isDefined === true
        wt.withdrawal.get.date.after(wt.deposit.date) === true
      }
    }
    "fail withdraw with invalid UUID" in {
      running(FakeApplication()) {
        Transaction.withdraw(new UUID(0, 0)) must throwA[NoSuchElementException]
        Try(Transaction.withdraw(new UUID(0, 0))).isSuccess === false
      }
    }
    "fail withdraw on already withdrawn transaction" in {
      running(FakeApplication()) {
        val t = Transaction.create(
          2290,
          Receiver("Huey Freeman", "10921923", "US"),
          Sender("Riley Freeman", "1290192031", "US", "Atlanta", "Bourgeoisie Lane", None, "ella@example.com"),
          "boondocks"
        )
        t.isDefined === true
        val wd = Transaction.withdraw(t.get.id).withdrawal.get.date
        Transaction.withdraw(t.get.id) must throwA[AlreadyWithdrawnException]
        Transaction.findById(t.get.id).get.withdrawal.get.date === wd
      }
    }
    "returns empty list when there are no transaction entries" in empty_set {
      running(FakeApplication()) {
        Transaction.findAll.isEmpty === true
      }
    }
    "generate transaction codes for entries" in {
      running(FakeApplication()) {
        val t = Transaction.create(
          620,
          Receiver("Mama Odi", "516789203", "JM"),
          Sender("Princess without Frog", "122798312", "US", "New Orleans", "Fracoise Ave 23", None, "louissianaprincess@example.com"),
          "dance baby"
        )
        t.isDefined === true
        t.get.transactionCode.length === 8
      }
    }
    "validate codes with secrets" in {
      running(FakeApplication()) {
        val secret = "la droga es mio"
        val t = Transaction.create(
          620,
          Receiver("Fidel Castro", "18786341", "CU"),
          Sender("Pablo Escobar", "15268390012", "CO", "Medellín", "Carrera 666A", None, "broke.pablo@example.com"),
          secret 
        )
        t.isDefined === true
        Transaction.validate(t.get.id, t.get.transactionCode, secret) === true
        Transaction.validate(t.get.id, t.get.transactionCode, "no") === false
        Transaction.validate(t.get.id, "12345678", secret) === false
      }
    }
    "finds all transactions by given tokens" in { pending }
    "finds all non-completed transactions by code" in { 
      running(FakeApplication()) {
        DB.withConnection { implicit connection =>
          def insertTransactionWithCode(code:String):List[UUID] = {
            SQL("""
              INSERT INTO TRANSACTIONS (
                amount,
                receiver_name,
                receiver_phonenumber,
                receiver_country,
                sender_name,
                sender_address,
                sender_phonenumber,
                sender_email,
                sender_city,
                sender_country,
                token,
                code 
              ) values (
                2000,
                'Kwame Foo',
                '+5978765432123456',
                'SR',
                'Mama Odi',
                'Karel Doormanstraat 41091',
                '+31765437890984',
                'sender@example.com',
                'Teststad',
                'NL',
                'blablah',
                {code}
              )"""
            ).on(
              'code -> code
            ).executeInsert(parser *)
          }
          val code = Stream.continually(Random.nextInt(10)).take(8).mkString
          val count = Random.nextInt(20)
          1 to count foreach(_ => insertTransactionWithCode(code))
          Transaction.findByCode(code).isEmpty === false
          Transaction.findByCode(code).length === count
        }
      }
    }
    "counts the amount of transactions" in {
      running(FakeApplication()) {
        DB.withConnection { implicit connection =>
          val old_count = Transaction.count
          Transaction.create(
            40,
            Receiver("Candice Cleare", "199283832", "AU"),
            Sender("Fernando da Gama", "2993939", "BR", "Sao Paolo", "Unknown", None, "f.gama@example.co.br"),
            "vishnu"
          )
          Transaction.count === old_count+1
        }
      }
    }
    "counts the non-completed transactions by code" in {
      running(FakeApplication()) {
        DB.withConnection { implicit connection =>
          def insertTransactionWithCode(code:String):List[UUID] = {
            SQL("""
              INSERT INTO TRANSACTIONS (
                amount,
                receiver_name,
                receiver_phonenumber,
                receiver_country,
                sender_name,
                sender_address,
                sender_phonenumber,
                sender_email,
                sender_city,
                sender_country,
                token,
                code 
              ) values (
                2000,
                'Kwame Foo',
                '+5978765432123456',
                'SR',
                'Mama Odi',
                'Karel Doormanstraat 41091',
                '+31765437890984',
                'sender@example.com',
                'Teststad',
                'NL',
                'blablah',
                {code}
              )"""
            ).on(
              'code -> code
            ).executeInsert(parser *)
          }

          val code = Stream.continually(Random.nextInt(10)).take(8).mkString
          val count = Random.nextInt(20)
          1 to count foreach(_ => insertTransactionWithCode(code))
          Transaction.countByCode(code) === count
        }
      }
    }
    "fails when adding transaction with empty transaction code" in { pending }
  }

  object empty_set extends Before {
    def before {
      running(FakeApplication()) {
        DB.withConnection { implicit c =>
          SQL("""DELETE FROM transactions""").executeUpdate()
        }
      }
    }
  }
}
