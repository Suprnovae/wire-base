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

object CashPoints extends BaseController {
  def getItem(id: Any) = SecureAction { implicit request =>
    implicit val cashpointWrites = new Writes[CashPoint] {
      def writes(t: CashPoint): JsValue = {
        Json.obj(
          "id"       -> JsString(t.id.toString),
          "active"   -> JsBoolean(t.active)
        )
      }
    }

    val uuid: UUID = id match {
      case k: UUID => k
      case k: String => UUID.fromString(k)
      case k => UUID.fromString(k.toString)
    }

    val point = CashPoint.findById(uuid)
    render {
      if(point.isDefined) {
        case Accepts.Html() => Ok
        case Accepts.Json() => Ok
      } else {
        case Accepts.Html() => NotFound
        case _              => NotFound
      }
    }
  }

  def history(id: Any) = SecureAction { implicit request =>
    // TODO: does user have clearance
    println(request.headers)
    // TODO: extract log of made transactions
    Ok
  }
}
