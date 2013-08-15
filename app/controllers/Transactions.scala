package controllers

import java.util.UUID
import models._
import play.api._
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

  def getItem(id: UUID) = TODO // return the overview
  def update(id: UUID, content:Transaction) = TODO //
  def remove(id: UUID) = TODO //
  def add(transaction: Transaction) = TODO //
  
}
