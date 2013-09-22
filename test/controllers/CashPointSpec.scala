package test.controllers

import anorm._
import java.awt.Point
import java.util.UUID
import models._
import controllers.CashPoints
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
    "render the result as json upon request" in {
      running(FakeApplication()) {
        val headers = FakeHeaders(Seq(
          "ACCEPT" -> Seq("application/json")
        ))
        val request = FakeRequest(GET, "/cashpoints", headers, "")
        val page = route(request).get
        status(page) must equalTo(OK)
        contentType(page) must beSome.which(_ == "application/json")
      }
    }

    "render the index page" in {
      running(FakeApplication()) {
        val page = route(FakeRequest(GET, "/cashpoints")).get
        status(page) must equalTo(OK)
        contentType(page) must beSome.which(_ == "text/html")
      }
    }
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

  "create a new resource" in {
    running(FakeApplication()) {
      val count = CashPoint.count
      val url = "/cashpoints"
      val params = Map(
        "serial"    -> "MA_RA_ATLAS",
        "note"      -> "Mobile pickup booth",
        "address"   -> "21 Avenue Al Atlas",
        "city"      -> "Rabat",
        "country"   -> "MA",
        "latitude"  -> "33.99652",
        "longitude" -> "-6.84668"
      )

      // TODO: Don't redefine forms in tests, call submitForm from controllers
      val form = CashPoints.submitForm
  
      form.bind(params).hasErrors must beFalse
      form.errors.size must equalTo(0)
  
      val page = route(FakeRequest(POST, url)
        .withFormUrlEncodedBody(params.toList: _*)
      ).get
  
      status(page) must equalTo(CREATED)
      CashPoint.count === count+1
    }
  }

    "be mutable by cashpoint admins only" in { todo }
    "should only present overview to cashpoint admins" in { todo }
    "should be creatable by wire admins" in { todo }
    "should allow the state to be modified by wire admins" in { todo }
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
