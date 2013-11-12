package test.controllers

import java.util.UUID
import models._
import org.apache.commons.codec.binary.Base64
import org.specs2.mutable._
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._

class UserSpec extends BaseSpecification {
  "/signup" should {
    val params = Map(
      "handle"        -> "bank@wire.local",
      "secret"        -> "MoneyMoneyMoneyMoney!",
      "secret_repeat" -> "MoneyMoneyMoneyMoney!"
    )
    val form = Form(
      mapping(
        "handle"        -> nonEmptyText,
        "secret"        -> nonEmptyText,
        "secret_repeat" -> nonEmptyText
      )(UserForm.apply)(UserForm.unapply)
    )
    val url = "/signup"

    "create a new resource" in empty_set {
      running(FakeApplication()) {
        val count = User.count

        form.bind(params).hasErrors must beFalse
        form.errors.size must equalTo(0)

        status(route(FakeRequest(POST, url, headers, "")
          .withFormUrlEncodedBody(params.toList: _*)
        ).get) must equalTo(CREATED)
        User.count === count+1
      }
    }

    "should fail if form is incomplete" in empty_set {
      running(FakeApplication()) {
        val count = User.count

        var currentParams = Map("handle" -> "cash@wire.local")
        form.bind(currentParams).hasErrors must beTrue

        status(route(FakeRequest(POST, url, headers, "")
          .withFormUrlEncodedBody(currentParams.toList: _*)
        ).get) must equalTo(BAD_REQUEST)

        currentParams = Map("secret" -> "Blahblahblah")
        status(route(FakeRequest(POST, url, headers, "")
          .withFormUrlEncodedBody(currentParams.toList: _*)
        ).get) must equalTo(BAD_REQUEST)

        currentParams = Map(
          "handle" -> "dollar@wire.local", 
          "secret" -> "I need dollars, dollars!"
        )
        status(route(FakeRequest(POST, url, headers, "")
          .withFormUrlEncodedBody(currentParams.toList: _*)
        ).get) must equalTo(BAD_REQUEST)

        User.count === count
      }
    }

    "should fail if verification password differs" in empty_set {
      running(FakeApplication()) {
        val count = User.count

        var currentParams = Map(
          "handle" -> "dollar@wire.local", 
          "secret" -> "I need dollars, dollars!",
          "secret_repeat" -> "Where is my money, Brian!"
        )

        //form.bind(currentParams).hasErrors must beTrue

        status(route(FakeRequest(POST, url, headers, "")
          .withFormUrlEncodedBody(currentParams.toList: _*)
        ).get) must equalTo(BAD_REQUEST)

        User.count === count
      }
    }

    "should fail if handle is taken" in empty_set {
      running(FakeApplication()) {
        User.create("bank@wire.local", "PoenPoenPoen").isSuccess === true
        form.bind(params).hasErrors must beFalse

        val count = User.count
        status(route(FakeRequest(POST, url, headers, "")
          .withFormUrlEncodedBody(params.toList: _*)
        ).get) must equalTo(BAD_REQUEST)
        User.count === count
      }
    }
  }
}
