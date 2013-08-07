import org.specs2.mutable._

import play.api.Play.current
import play.api.db.DB
import play.api.test._
import play.api.test.Helpers._

import anorm._
import anorm.SqlParser._

import java.util.{UUID, Date}
import scala.util.Try

class TransactionSpec extends Specification {
  "Transaction" should {
    "be retrieved by id" in {
      running(FakeApplication()) {
        implicit def rowToUUID: Column[UUID] = {
          Column.nonNull[UUID] { (value, meta) =>
            value match {
              case uuid: UUID     => Right(uuid)
              case string: String => Right(UUID.fromString(string))
            }
          }
        }
        val parser = { get[UUID]("id") }
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
        //val Some(transaction) = Computer.findById(new_id)
      }
    }
    "be instantiable" in {
      val transaction = Transaction (
        Some(UUID.randomUUID),
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
    "should be withdrawable" in {
      running(FakeApplication()) {
        val t = Transaction.create(
          400,
          Receiver("Christopher Wallace", "1238293842", "US"),
          Sender("Ella Fitzgerald", "2838384848", "US", "Atlanta", "Bourgeoisie Lane", None, "ella@example.com"),
          "boondocks"
        )
        t.isDefined === true
        val id = t.get.id
        //Transaction.withdraw(t.get.id).withdrawn_at.getClass.isInstanceOf[String] === true
        Transaction.findById(id.get).get.withdrawal.isDefined === false
        val wt = Transaction.withdraw(id.get)
        wt.withdrawal.isDefined === true
        println("The before is " + wt.deposit.date + " and the after is " + wt.withdrawal.get.date)
        wt.withdrawal.get.date.after(wt.deposit.date) === true
      }
    }
  }
}

trait table extends After {
  def after = {
    DB.withConnection { implicit connection =>
      SQL("DELETE FROM transactions")
    }
  }
}
