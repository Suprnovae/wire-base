package controllers

import java.util.UUID
import models._
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json._
import play.api.mvc._
import views._

object Transactions extends Controller {

  def index(page: Int = 0, maxItems: Int = 100) = Action { implicit request =>
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

    val transactions = Transaction.findAll
    render {
      case Accepts.Html() => Ok(html.transactions.index(transactions))
      case Accepts.Json() => Ok(Json.toJson(transactions))
    }
  }

  def getItem(id: Any) = Action { implicit request =>
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
        case Accepts.Html() => Ok(html.transactions.detail(transaction.get))
        case Accepts.Json() => Ok(Json.toJson(transaction.get))
      } else {
        case Accepts.Html() => NotFound(html.common.notfound())
        case _              => NotFound(Json.toJson("Not found"))
      }
    }
  }

  def remove(id: UUID) = TODO //

  val submitForm = Form(
    tuple(
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
    )
  )

  def add = Action { implicit request =>
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
      formWithErrors => BadRequest("Oh shit"),
      {
        case (
          amount,
          payment,
          secret,
          sender_name,
          sender_address,
          sender_city,
          sender_country,
          receiver_name,
          receiver_mobile,
          receiver_country
        ) => {
          val t = Transaction.create(
            amount,
            Receiver(receiver_name, receiver_mobile, receiver_country),
            Sender(sender_name, "", sender_country, sender_city, sender_address, None, ""),
            secret
          )
          if(t.isDefined) {
            request match {
              case Accepts.Html() => Ok(html.transactions.detail(t.get))
              case Accepts.Json() => Ok(Json.toJson(t.get))
            }
          } else {
            request match {
              case Accepts.Html() => BadRequest("failed")
              case _              => BadRequest("crap")
            }
          }
        }
      }
    )
  }

  def update(id: UUID, content:Transaction) = TODO //

}
