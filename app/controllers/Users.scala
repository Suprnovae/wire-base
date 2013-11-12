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

object Users extends BaseController {
  val signupForm = Form(
    tuple(
      "handle" -> email,
      "secret" -> nonEmptyText,
      "secret_repeat" -> nonEmptyText
    ) verifying("Invalid handle or password", fields => fields match {
      case (e, s, r) => User.findByHandle(e).isEmpty && (s == r)
    })
  )

  def index() = SecureAction { implicit request =>
    implicit val userWrites = new Writes[User] {
      def writes(u: User): JsValue = {
        Json.obj(
          "id"       -> JsString(u.id.toString),
          "handle"   -> JsString(u.handle.toString)
        )
      }
    }

    if((request getUser).isDefined) {
      Ok(html.users.index((request getUser)))
    } else {
      BadRequest
    }
  }

  def add = SecureAction { implicit request =>
    implicit val userWrites = new Writes[User] {
      def writes(u: User): JsValue = {
        Json.obj(
          "id"     -> JsString(u.id.toString),
          "handle" -> u.handle,
          "status" -> u.status
        )
      }
    }

    signupForm.bindFromRequest.fold(
      formWithErrors => {
        request match {
          case _ => BadRequest
        }
      },
      validForm => {
        val u = User.create("user@example.com", "secret")
        if(u.isSuccess) {
          request match {
            case Accepts.Html() => Created(html.users.index(Some(u.get)))
            case Accepts.Json() => Created(Json.toJson(u.get))
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
}
