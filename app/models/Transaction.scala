import anorm._
import anorm.SqlParser._
import com.github.t3hnar.bcrypt._
import java.sql.Timestamp
import java.util.Date
import java.util.UUID
import play.api.db.DB
import play.api.Play.current
import scala.math.BigDecimal
import scala.util.Random

abstract class Event

case class Deposit(date: Date) extends Event

case class Withdrawal(date: Date) extends Event

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
  sender: Sender,
  deposit: Deposit,
  withdrawal: Option[Withdrawal],
  tokenHash: String,
  transactionCode: String
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

  implicit def rowToDate: Column[Date] = {
    Column.nonNull[Date] { (value, meta) => 
      value match {
        case string: String => Right(new Date(Timestamp.valueOf(string).getTime))
        case stamp: Timestamp => Right(new Date(stamp.getTime()))
        case _ => Right(new Date(0L))
      }
    }
  }

  implicit def rowToBigDecimal: Column[BigDecimal] = {
    Column.nonNull[BigDecimal] { (value, meta) => 
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
    get[String]("transactions.sender_country")~
    get[Date]("transactions.deposited_at")~
    get[Option[Date]]("transactions.withdrawn_at")~
    get[String]("transactions.token")~
    get[String]("transactions.code") map {
      case id~v~rn~rp~rc~sn~sa~sp~se~sc~sl~td~tw~t~c => 
      Transaction(id, v,
        Receiver(rn, rp, rc),
        Sender(sn, sp, sl, sc, sa, None, se),
        Deposit(td), //td
        if (tw.isDefined) Some(Withdrawal(tw.get)) else None,
        t,
        c
      )
    }
  }

  def findAll(): Seq[Transaction] = {
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM transactions").as(Transaction.simple *)
    }
  }

  def findById(id: UUID): Option[Transaction] = {
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM transactions WHERE id = {id}").on(
        'id -> id
      ).as(Transaction.simple.singleOpt)
    }
  }
  def findByTokens(code: String, secret: String): Option[Transaction] = None
  def validate(id: UUID, code: String, secret: String): Boolean = {
    Transaction.findById(id) match {
      case Some(t) => if ((t.receiver.name+secret+t.transactionCode).isBcrypted(t.tokenHash)) true else false
      case None    => false
    }
  }
  // TODO: make this a Try(Transaction)
  def withdraw(id: UUID):Transaction = {
    DB.withConnection { implicit c => 
      val res = SQL("""
        UPDATE transactions
        SET withdrawn_at = CURRENT_TIMESTAMP
        WHERE id = {id}""").on('id -> id).executeUpdate()
      Transaction.findById(id).get
    }
  }
  def create(
    amount: Int,
    receiver: Receiver,
    sender: Sender,
    secret: String
    ): Option[Transaction] = {
    DB.withConnection { implicit connection => 
      val randomCode = Stream.continually(Random.nextInt(10)).take(8).mkString
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
          sender_country,
          token,
          code
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
          {sender_country},
          {token},
          {code}
        )"""
      ).on(
        'amount               -> amount,
        'receiver_name        -> receiver.name,
        'receiver_phonenumber -> receiver.phonenumber,
        'receiver_country     -> receiver.country,
        'sender_name          -> sender.name,
        'sender_address       -> sender.address,
        'sender_phonenumber   -> sender.phonenumber,
        'sender_email         -> sender.email,
        'sender_city          -> sender.city,
        'sender_country       -> sender.country,
        'token                -> (receiver.name+secret+randomCode).bcrypt,
        'code                 -> randomCode
      ).executeInsert[List[Transaction]](Transaction.simple *)
      Transaction.findById(res.head.id)
    }
  }
}
