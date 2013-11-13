package test.controllers

import anorm._
import java.awt.Point
import java.awt.geom.Point2D
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

class WithdrawalSpec extends BaseSpecification {
  "/withdrawals" should {
    "is processed" in empty_set {
      running(FakeApplication()) {
        var t = Transaction.create(
          40000,
          Receiver("Daisy Buchanan", "686278912", "US"),
          Sender("Jay Gatsby", "199288333", "US", "New York", "Unknown", None, "jay@gatsby.com"),
          "green light"
        )
        val p = CashPoint.create(
          "US_NYC_MANH_WALLSTR_0012",
          Location(new Point(232, 433), "Wall Street 12", "New York City", "US"),
          Some("The ATM near the NYSE at 11 Wall Street")
        )

        val params = Map(
          "cashpoint_id"   -> p.get.id.toString,
          "code"           -> t.get.transactionCode,
          "secret"         -> "green light"
        )

        val count = Withdrawal.count

        val url = "/withdrawals"
        val page = route(FakeRequest(POST, url, headers, "")
          .withFormUrlEncodedBody(params.toList: _*)
        ).get

        status(page) must equalTo(CREATED)
        Withdrawal.count === count+1
      }
    }
  }
}
