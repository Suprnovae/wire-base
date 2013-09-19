package test.controllers

import anorm._
import java.awt.Point
import java.util.UUID
import models._
import org.specs2.mutable._
import play.api.data._
import play.api.data.Forms._
import play.api.db.DB
import play.api.mvc._
import play.api.Play.current
import play.api.test._
import play.api.test.Helpers._

class CashPointSpec extends Specification {
  "/cashpoints" should {
    "render the result as json upon request" in { todo }
    "render the index page" in { todo }
    "send 404 on requesting details with non-existent id" in { todo }
    "returns the resource" in empty_set {
      running(FakeApplication()) {
        val p = CashPoint.create(
          "US_NYC_MANH_WALLSTR_0012",
          Location(new Point(232, 433), "Wall Street 12", "New York City", "US"),
          Some("The ATM near the NYSE at 11 Wall Street")
        )
  
        p.isDefined === true
        val uuid = p.get.id
        val headers = FakeHeaders(Seq("ACCEPT" -> Seq("application/json")))
  
        val url = "/cashpoints/" + uuid.toString
        var page = route(FakeRequest(GET, url)).get
        status(page) must equalTo(OK)
        contentType(page) must beSome.which(_ == "text/html")
  
        page = route(FakeRequest(GET, url, headers, "")).get
        status(page) must equalTo(OK)
        contentType(page) must beSome.which(_ == "application/json")
      }
    }
  }

  object empty_set extends Before {
    def before {
      running(FakeApplication()) {
        DB.withConnection { implicit c =>
          SQL("""DELETE FROM withdrawals""").executeUpdate()
          SQL("""DELETE FROM transactions""").executeUpdate()
          SQL("""DELETE FROM cash_points""").executeUpdate()
        }
      }
    }
  }
}
