package controllers

import java.awt.geom.Point2D
import java.util.UUID
import models._
import org.apache.commons.lang3.StringUtils
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.libs.json._
import play.api.mvc._
import play.api.Play.current
import scala.util._
import views._

object CashPoints extends BaseController {
  val submitForm = Form(
    mapping(
      "serial" -> nonEmptyText,
      "note"   -> nonEmptyText,
      "address" -> nonEmptyText,
      "city" -> nonEmptyText,
      "country" -> nonEmptyText,
      "longitude" -> of[Float],
      "latitude" -> of[Float] 
    )(CashPointForm.apply)(CashPointForm.unapply)
  )

  def index = SecureAction { implicit request =>
    implicit val cashpointWrites = new Writes[CashPoint] {
      def writes(t: CashPoint): JsValue = {
        Json.obj(
          "id"       -> JsString(t.id.toString),
          "serial"   -> JsString(t.serial)
        )
      }
    }

    request getUserCredentials
    val cashpoints = CashPoint.findAll
    render {
      case Accepts.Html() => Ok(html.cashpoints.index(cashpoints, request getUser))
      case Accepts.Json() => Ok(Json.toJson(cashpoints))
    }
  }

  def add = SecureAction { implicit request =>
    implicit val cashpointWrites = new Writes[CashPoint] {
      def writes(p: CashPoint): JsValue = {
        Json.obj(
          "id"       -> JsString(p.id.toString),
          "serial"   -> p.serial,
          "location" -> Json.obj(
            "address"   -> JsString(p.location.address),
            "city"      -> JsString(p.location.city),
            "country"   -> JsString(p.location.country)
          ),
          "note"     -> JsString(p.note.get)
        )
      }
    }
    submitForm.bindFromRequest.fold(
      formWithErrors => BadRequest,
      validForm => {
        val p = CashPoint.create(
          validForm.serial,
          Location(
            new Point2D.Float(validForm.latitude, validForm.longitude),
            validForm.address,
            validForm.city,
            validForm.country
          ),
          Some(validForm.note)
        )
        if(p.isDefined) {
          request match {
            case Accepts.Html() => Created //(html.cashpoints.detail(p.get, request getUser))
            case Accepts.Json() => Created(Json.toJson(p.get))
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

  def getItem(id: Any) = SecureAction { implicit request =>
    implicit val cashpointWrites = new Writes[CashPoint] {
      def writes(t: CashPoint): JsValue = {
        Json.obj(
          "id"       -> JsString(t.id.toString),
          "serial"   -> JsString(t.serial)
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
        case Accepts.Html() => Ok(html.cashpoints.detail(point.get, request getUser, Withdrawal.findByCashPoint(point.get)))
        case Accepts.Json() => Ok(Json.toJson(point.get))
      } else {
        case Accepts.Html() => NotFound(html.common.notfound(request getUser))
        case _              => NotFound(Json.toJson("Not found"))
      }
    }
  }

  def modify(id: Any, active: Boolean) = SecureAction { implicit request =>
    implicit val cashpointWrites = new Writes[CashPoint] {
      def writes(t: CashPoint): JsValue = {
        Json.obj(
          "id"       -> JsString(t.id.toString),
          "serial"   -> JsString(t.serial)
        )
      }
    }

    val uuid: UUID = id match {
      case k: UUID => k
      case k: String => UUID.fromString(k)
      case k => UUID.fromString(k.toString)
    }
    
    if(CashPoint.modify(uuid, active).isDefined) {
      request match {
        case Accepts.Html() => Ok //(html.cashpoints.detail(CashPoint.findById(uuid).get, request getUser, Withdrawal.findByCashPoint()))
        case Accepts.Json() => Ok(Json.toJson(CashPoint.findById(uuid).get))
      }
    } else {
      Conflict
    }
  }

  def history(id: Any) = SecureAction { implicit request =>
    // TODO: does user have clearance
    println(request.headers)
    // TODO: extract log of made transactions
    Ok
  }
}
