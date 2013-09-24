package controllers

import java.util.UUID
import models._
import org.apache.commons.codec.binary.Base64
import org.apache.commons.lang3.StringUtils
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json._
import play.api.mvc._
import play.api.Play.current
import scala.util._
import views._

class BaseController extends Controller {

  implicit class UserHelper(request: Request[AnyContent]) {
    def getUserCredentials: List[String] = {
      val a = request.headers.get("Authorization").get.split(" ").drop(1)
      new String(Base64.decodeBase64(a.headOption.getOrElse("").getBytes)).split(":").toList
    }

    def validateUser: Boolean  = {
      println(request.getUserCredentials)
      request.getUserCredentials match {
        case u :: p :: Nil => User.validate(u, p)
        case _ => false
      }
    }

    // TODO: Test this baby
    def getUser: Option[User] = {
      if(request.validateUser) {
        User.findByHandle(request.getUserCredentials.headOption.getOrElse(""))
      } else {
        None
      }
    }
  }

  def SecureAction(f: Request[AnyContent] => Result): Action[AnyContent] = {
    Action { request =>
      val hdr = request.headers.get("x-forwarded-proto")
      if((Play.isProd && (hdr.isDefined && StringUtils.contains(hdr.get, "https"))) || Play.isDev || Play.isTest) {
        if(request.validateUser) {
          f(request)
        } else {
          Unauthorized.withHeaders("WWW-Authenticate" -> """Basic realm="Secured"""")
        }
      } else {
        // TODO: Redirect to SSL in Production
        if(Play.isProd) {
          Redirect("https://"+request.host+request.uri)
        } else {
          f(request)
        }
      }
    }
  }
}
