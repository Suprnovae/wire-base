import org.specs2.mutable._

import play.api.Play.current
import play.api.db.DB
import play.api.test._
import play.api.test.Helpers._

import anorm._
import anorm.SqlParser._

import java.util.UUID

class TransactionSpec extends Specification {
  "Transaction model" should {
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
          val new_ids:List[UUID] = SQL("""
            INSERT INTO TRANSACTIONS
              (
                amount,
                receiver_name,
                receiver_phonenumber,
                receiver_country,
                sender_name,
                sender_address,
                sender_phonenumber,
                sender_email,
                sender_city,
                sender_country
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
                'NL'
              )
          """).on(
            "amount"   -> 420,
            "receiver" -> "Eline Weson",
            "sender"   -> "Orgeon Waterberg").executeInsert(parser *)
          println("The uuid for the new record is " + new_ids)
        }
        //val Some(transaction) = Computer.findById(new_id)
      }
    }
  }
}
