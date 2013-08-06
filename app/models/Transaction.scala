import anorm._
import anorm.SqlParser._
import play.api.db.DB
import play.api.Play.current
import java.util.UUID
import scala.math.BigDecimal

case class Receiver(
  name: String,
  phonenumber: String,
  country: String
)

case class Sender(
  name: String,
  phonenumber: String,
  country: String,
  city: String,
  address: String,
  postalcode: Option[String],
  email: String
)

case class Transaction(
  id: UUID,
  amount: BigDecimal,
  receiver: Receiver,
  sender: Sender
)

object Transaction {
  implicit def rowToUUID: Column[UUID] = {
    Column.nonNull[UUID] { (value, meta) =>
      value match {
        case uuid: UUID     => Right(uuid)
        case string: String => Right(UUID.fromString(string))
      }
    }
  }
  implicit def rowToBigDecimal: Column[BigDecimal] = {
    Column.nonNull[BigDecimal] { (value, meta) => 
      println("Whow she is a " + value.getClass)
      value match {
        case value:Double => Right(BigDecimal.double2bigDecimal(value))
        case value:Long   => Right(BigDecimal.long2bigDecimal(value))
        case value:java.math.BigDecimal => Right(new BigDecimal(value))
        case _            => Right(BigDecimal.int2bigDecimal(0))
      }
    }
  }

  val simple = {
    get[UUID]("transactions.id")~
    get[BigDecimal]("transactions.amount")~
    get[String]("transactions.receiver_name")~
    get[String]("transactions.receiver_phonenumber")~
    get[String]("transactions.receiver_country")~
    get[String]("transactions.sender_name")~
    get[String]("transactions.sender_address")~
    get[String]("transactions.sender_phonenumber")~
    get[String]("transactions.sender_email")~
    get[String]("transactions.sender_city")~
    get[String]("transactions.sender_country") map {
      case id~v~rn~rp~rc~sn~sa~sp~se~sc~sl => Transaction(id, v,
        Receiver(rn, rp, rc),
        Sender(sn, sp, sl, sc, sa, None, se)
      )
    }
  }
  def all(): List[Transaction] = List[Transaction]()
  def findById(id: Any): Option[Transaction] = {
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM transactions WHERE id = {id}").on(
        'id -> id
      ).as(Transaction.simple.singleOpt)
    }
  }
  def findByTokens(code: String, secret: String): Option[Transaction] = None
  def complete() {}
  def create(transaction: Transaction): Transaction = {
    DB.withTransaction { implicit connection => 
      val res = SQL("""
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
          sender_country
        ) values (
          {amount},
          {receiver_name},
          {receiver_phonenumber},
          {receiver_country},
          {sender_name},
          {sender_address},
          {sender_phonenumber},
          {sender_email},
          {sender_city},
          {sender_country}
        )"""
      ).on(
        'amount               -> transaction.amount,
        'receiver_name        -> transaction.receiver.name,
        'receiver_phonenumber -> transaction.receiver.phonenumber,
        'receiver_country     -> transaction.receiver.country,
        'sender_name          -> transaction.sender.name,
        'sender_address       -> transaction.sender.address,
        'sender_phonenumber   -> transaction.sender.phonenumber,
        'sender_email         -> transaction.sender.email,
        'sender_city          -> transaction.sender.city,
        'sender_country       -> transaction.sender.country
      ).executeInsert[List[Transaction]](Transaction.simple *)
      println("Some information " + res)
      transaction
    }
  }
}

// find helper
// find by transaction code and token
// find by id
// fields 
// save helper
// destroy helper
