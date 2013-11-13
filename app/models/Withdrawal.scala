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

case class WithdrawalForm(
  secret: String,
  code: String,
  cashpoint_id: String
)

case class Withdrawal(
  id: UUID,
  transaction_id: UUID,
  cashpoint_id: UUID,
  date: Date
)

case class DetailedWithdrawal(
  id: UUID,
  transaction_id: UUID,
  cashpoint_id: UUID,
  withdrawal_date: Date,
  deposit_date: Date,
  amount: BigDecimal,
  receiver: Receiver
)

object Withdrawal extends Model {
  val complete = {
    get[UUID]("withdrawals.id")~
    get[UUID]("withdrawals.transaction_id")~
    get[UUID]("withdrawals.cash_point_id")~
    get[Date]("withdrawals.created_at")~
    get[Date]("transactions.deposited_at")~
    get[BigDecimal]("transactions.amount")~
    get[String]("transactions.receiver_name")~
    get[String]("transactions.receiver_phonenumber")~
    get[String]("transactions.receiver_country") map {
      case id~t~cp~c~d~a~rn~rp~rc => 
      DetailedWithdrawal(id, t, cp, c, d, a,
        Receiver(rn, rp, rc)
      )
    }
  }
  val simple = {
    get[UUID]("withdrawals.id")~
    get[UUID]("withdrawals.transaction_id")~
    get[UUID]("withdrawals.cash_point_id")~
    get[Date]("withdrawals.created_at") map {
      case id~t~cp~c => 
      Withdrawal(id, t, cp, c)
    }
  }

  def findAll(): Seq[Withdrawal] = {
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM withdrawals").as(Withdrawal.simple *)
    }
  }

  def findById(id: UUID): Option[Withdrawal] = {
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM withdrawals WHERE id = {id}").on(
        'id -> id
      ).as(Withdrawal.simple.singleOpt)
    }
  }
  def findByTransactionId(id: UUID): Option[Withdrawal] = {
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM withdrawals WHERE transaction_id = {id}").on(
        'id -> id
      ).as(Withdrawal.simple.singleOpt)
    }
  }
  def findByCashPoint(cash_point: CashPoint): Seq[DetailedWithdrawal] = {
    DB.withConnection { implicit c =>
      SQL("""
        SELECT 
          withdrawals.id, 
          withdrawals.transaction_id, 
          withdrawals.cash_point_id, 
          withdrawals.created_at,
          transactions.amount,
          transactions.receiver_name,
          transactions.receiver_phonenumber,
          transactions.receiver_country,
          transactions.deposited_at
        FROM withdrawals 
        INNER JOIN transactions ON (withdrawals.transaction_id = transactions.id)
        WHERE cash_point_id = {id}""").on(
        'id -> cash_point.id
      ).as(Withdrawal.complete *)
    }
  }
  def count: Long = {
    DB.withConnection { implicit c =>
      SQL("""SELECT COUNT(*) AS c FROM withdrawals""")
        .apply().head[Long]("c")
    }
  }
  // TODO: make this a Try(Transaction)
  def create(
    transaction: Transaction,
    cash_point: CashPoint
    ): Option[Withdrawal] = {
    DB.withConnection { implicit connection => 
      val res = SQL("""
        INSERT INTO withdrawals (
          transaction_id,
          cash_point_id
        ) values (
          {transaction},
          {cash_point}
        )"""
      ).on(
        'transaction -> transaction.id,
        'cash_point  -> cash_point.id
      ).executeInsert[List[Withdrawal]](Withdrawal.simple *)
      Withdrawal.findById(res.head.id)
    }
  }
}
