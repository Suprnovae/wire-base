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

class BaseController extends Controller {

  def SecureAction(f: Request[AnyContent] => Result): Action[AnyContent] = {
    Action { request =>
      val hdr = request.headers.get("x-forwarded-proto")
      if((Play.isProd && (hdr.isDefined && StringUtils.contains(hdr.get, "https"))) || Play.isDev || Play.isTest) {
        request.headers.get("Authorization").flatMap { auth =>
          auth.split(" ").drop(1).headOption.filter { encoded =>
            new String(org.apache.commons.codec.binary.Base64.decodeBase64(encoded.getBytes)).split(":").toList match {
              case u :: p :: Nil => User.validate(u, p)
              case _ => false
            }
          }.map(_ => f(request))
        }.getOrElse {
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
