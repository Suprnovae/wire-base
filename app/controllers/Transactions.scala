package controllers

import java.util.UUID
import models._
import play.api._
import play.api.libs.json._
import play.api.mvc._
import views._

object Transactions extends Controller {
  
  implicit val transactionWrites = new Writes[Transaction] {
    def writes(t: UUID): JsString = new JsString(t.toString)
    def writes(t: Transaction): JsValue = {
      Json.obj(
        "amount" -> t.amount,
        "id"     -> JsString(t.id.toString)
      )
    }
  }

  def index(page: Int = 0, maxItems: Int = 100) = Action { implicit request =>
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
