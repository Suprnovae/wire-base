package controllers

import java.util.UUID
import models._
import org.apache.commons.lang3.StringUtils
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json._
import play.api.mvc._
import play.api.Play.current
import scala.util._
import views._

object Transactions extends BaseController {
  def index(code: String, secret: String, page: Int = 0, maxItems: Int = 100) = SecureAction { implicit request =>
    implicit val transactionWrites = new Writes[Transaction] {
      def writes(t: Transaction): JsValue = {
        Json.obj(
          "id"       -> JsString(t.id.toString),
          "amount"   -> t.amount,
          "receiver" -> JsString(t.receiver.name),
          "sender"   -> JsString(t.sender.name),
          "date"     -> JsString(t.deposit.date.toString)
        )
      }
    }

    if(code.isEmpty && secret.isEmpty) {
      val transactions = Transaction.findAll
      render {
        case Accepts.Html() => Ok(html.transactions.index(transactions, request getUser))
        case Accepts.Json() => Ok(Json.toJson(transactions))
      }
    } else {
      val transaction = Transaction.findByTokens(code, secret)
      if(transaction.isDefined) {
        render {
          case Accepts.Html() => Ok(html.transactions.detail(transaction.get, request getUser))
          case Accepts.Json() => Ok(Json.toJson(transaction.get))
        }
      } else {
        BadRequest
      }
    }
  }

  def getItem(id: Any) = SecureAction { implicit request =>
    implicit val transactionWrites = new Writes[Transaction] {
      def writes(t: Transaction): JsValue = {
        Json.obj(
          "id"       -> JsString(t.id.toString),
          "amount"   -> t.amount,
          "receiver" -> JsString(t.receiver.name),
          "sender"   -> JsString(t.sender.name),
          "date"     -> JsString(t.deposit.date.toString)
        )
      }
    }

    val uuid: UUID = id match {
      case k: UUID => k
      case k: String => UUID.fromString(k)
      case k => UUID.fromString(k.toString)
    }

    val transaction = Transaction.findById(uuid)
    render {
      if(transaction.isDefined) {
        case Accepts.Html() => Ok(html.transactions.detail(transaction.get, request getUser))
        case Accepts.Json() => Ok(Json.toJson(transaction.get))
      } else {
        case Accepts.Html() => NotFound(html.common.notfound(request getUser))
        case _              => NotFound(Json.toJson("Not found"))
      }
    }
  }

  def remove(id: UUID) = TODO //

  val submitForm = Form(
    mapping(
      "amount" -> number,
      "payment" -> number,
      "secret" -> nonEmptyText,
      "sender_name" -> nonEmptyText,
      "sender_address" -> nonEmptyText,
      "sender_city" -> nonEmptyText,
      "sender_country" -> nonEmptyText,
      "receiver_name" -> nonEmptyText, 
      "receiver_mobile" -> nonEmptyText,
      "receiver_country" -> nonEmptyText
    )(TransactionForm.apply)(TransactionForm.unapply)
  )

  def add = SecureAction { implicit request =>
    implicit val transactionWrites = new Writes[Transaction] {
      def writes(t: Transaction): JsValue = {
        Json.obj(
          "id"       -> JsString(t.id.toString),
          "amount"   -> t.amount,
          "receiver" -> Json.obj(
            "name"   -> JsString(t.receiver.name)
          ),
          "sender"   -> JsString(t.sender.name),
          "date"     -> JsString(t.deposit.date.toString)
        )
      }
    }

    submitForm.bindFromRequest.fold(
      formWithErrors => BadRequest, //(html.transactions.testform(formWithErrors)),
      validForm => {
        val t = Transaction.create(
          validForm.amount,
          Receiver(
            validForm.receiver_name,
            validForm.receiver_mobile,
            validForm.receiver_country
          ),
          Sender(
            validForm.sender_name,
            "",
            validForm.sender_country,
            validForm.sender_city,
            validForm.sender_address,
            None,
            ""
          ),
          validForm.secret
        )
        if(t.isDefined) {
          request match {
            case Accepts.Html() => Created(html.transactions.detail(t.get, request getUser))
            case Accepts.Json() => Created(Json.toJson(t.get))
          }
        } else {
          request match {
            case Accepts.Html() => BadRequest("failed")
            case _              => BadRequest("crap")
          }
        }
      }
    )
  }

  //def form = Ok(html.transactions.form)
  def withdraw(id: Any, code: String, secret: String, cash_point: Any) = SecureAction { implicit r =>
    implicit val transactionWrites = new Writes[Transaction] {
      def writes(t: Transaction): JsValue = {
        Json.obj(
          "id"       -> JsString(t.id.toString),
          "amount"   -> t.amount,
          "receiver" -> JsString(t.receiver.name),
          "sender"   -> JsString(t.sender.name),
          "date"     -> JsString(t.deposit.date.toString)
        )
      }
    }

    val uuid: UUID = id match {
      case k: UUID => k
      case k: String => UUID.fromString(k)
      case k => UUID.fromString(k.toString)
    }
    val point: Option[CashPoint] = cash_point match {
      case k: UUID => CashPoint.findById(k)
      case k: String => CashPoint.findById(UUID.fromString(k))
      case k => CashPoint.findById(UUID.fromString(k.toString))
    }

    val withdrawalAttempt: Try[Withdrawal] = {
      if(Transaction.validate(uuid, code, secret)) {
        Try(Withdrawal.create(Transaction.findById(uuid).get, point.get).get)
      } else {
        Try(throw new Exception("Transaction tokens not validated"))
      }
    }

    if(withdrawalAttempt.isSuccess) {
      Ok
    } else {
      Conflict
    }
  }

}
