package controllers

import play.api._
import models._
import play.api.mvc._

object Application extends BaseController {
  
  def index = Action { implicit request =>
    Ok(views.html.index("Your new application is ready.", request getUser))
  }
  
}
