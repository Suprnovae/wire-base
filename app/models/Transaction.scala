package models

import anorm._
import anorm.SqlParser._
import com.github.t3hnar.bcrypt._
import java.sql.Timestamp
import java.util.{ Date, UUID }
import play.api.db.DB
import play.api.Play.current
import scala.math.BigDecimal
import scala.util.Random

abstract class Event

case class Deposit(date: Date) extends Event

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

case class TransactionForm(
  amount: Int,
  payment: Int,
  secret: String,
  sender_name: String,
  sender_address: String,
  sender_city: String,
  sender_country: String,
  receiver_name: String,
  receiver_mobile: String,
  receiver_country: String
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

object Transaction extends Model {
  val complete = {
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
        Withdrawal.findByTransactionId(id),
        t,
        c
      )
    }
  }
  val simple = complete

  def findAll(): Seq[Transaction] = {
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM transactions").as(Transaction.simple *)
    }
  }

  def findByCode(code: String): Seq[Transaction] = {
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM transactions WHERE code = {code}")
      .on('code -> code).as(Transaction.simple *)
    }
  }

  def findById(id: UUID): Option[Transaction] = {
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM transactions WHERE id = {id}").on(
        'id -> id
      ).as(Transaction.simple.singleOpt)
    }
  }
  def findByTokens(code: String, secret: String): Option[Transaction] = {
    DB.withConnection { implicit c =>
      val result = SQL("""
        SELECT * FROM transactions
        WHERE code = {code} AND withdrawn_at IS NULL
      """)
      .on('code -> code)
      .as(Transaction.simple.singleOpt)

      if(result.isDefined && Transaction.validate(result.get.id, code, secret)) {
        result
      } else {
        None
      }
    }
  }
  def count: Long = {
    DB.withConnection { implicit c =>
      SQL("""SELECT COUNT(*) AS c FROM transactions""")
        .apply().head[Long]("c")
    }
  }
  def countByCode(code: String): Long = {
    DB.withConnection { implicit c =>
      SQL("""SELECT COUNT(*) AS c FROM transactions WHERE code = {code}""")
        .on('code -> code).apply().head[Long]("c")
    }
  }
  def validate(id: UUID, code: String, secret: String): Boolean = {
    Transaction.findById(id) match {
      case Some(t) => if ((t.receiver.name+secret+code).isBcrypted(t.tokenHash)) true else false
      case None    => false
    }
  }
  def withdraw(id: UUID, cash_point: CashPoint): Transaction = {
    DB.withConnection { implicit c => 
      val count = SQL("""
        UPDATE transactions
        SET withdrawn_at = CURRENT_TIMESTAMP, withdrawn_by = {cash_point}
        WHERE id = {id} AND withdrawn_at IS NULL""")
        .on('id -> id, 'cash_point -> cash_point.id)
        .executeUpdate()
      val bogey = Transaction.findById(id)
      if((count==0) && (bogey != None)) throw new AlreadyWithdrawnException()
      // FIX: What happens if a non-existent id is withdrawn as we're not returning Options
      bogey.get
    }
  }
  // TODO: make this a Try(Transaction)
  def create(
    amount: Int,
    receiver: Receiver,
    sender: Sender,
    secret: String
    ): Option[Transaction] = {
    DB.withConnection { implicit connection => 
      var randomCode: String = ""
      do {
        randomCode = Stream.continually(Random.nextInt(10)).take(8).mkString
      } while(Transaction.countByCode(randomCode) > 0)

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

class AlreadyWithdrawnException extends Exception
