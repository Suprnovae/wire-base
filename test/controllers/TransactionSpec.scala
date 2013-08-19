package test.controllers

import java.util.UUID
import models._
import org.specs2.mutable._
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._

class TransactionSpec extends Specification {
  
  "/transactions" should {

    "render the result as json upon request" in {
      pending
      running(FakeApplication()) {
        val headers = FakeHeaders(Seq("ACCEPT" -> Seq("application/json")))
        val request = FakeRequest(GET, "/transactions", headers, "")
        val page = route(request).get
        status(page) must equalTo(OK)
        contentType(page) must beSome.which(_ == "application/json")
      }
    }

    "render the index page" in {
      running(FakeApplication()) {
        val page = route(FakeRequest(GET, "/transactions")).get
        status(page) must equalTo(OK)
        contentType(page) must beSome.which(_ == "text/html")
      }
    }

    "send 404 on requesting details with non-existent id" in {
      running(FakeApplication()) {
        // html
        val uuid = (new UUID(0, 0)).toString
        val request = FakeRequest(GET, "/transactions/" + uuid)
        status(route(request).get) must equalTo(NOT_FOUND)

        val page = route(request).get
        contentType(page) must beSome.which(_ == "text/html")
      }
    }

    "send 404 on requesting json details with non-existing id" in {
      running(FakeApplication()) {
        // json
        val uuid = (new UUID(0, 0)).toString
        val headers = FakeHeaders(Seq("ACCEPT" -> Seq("application/json")))
        val request = FakeRequest(GET, "/transactions/" + uuid, headers, "")

        val page = route(request).get
        status(page) must equalTo(NOT_FOUND)
        contentType(page) must beSome.which(_ == "application/json")
      }
    }

    "returns the resource" in {
      running(FakeApplication()) {
        val t = Transaction.create(
          40000,
          Receiver("Daisy Buchanan", "686278912", "US"),
          Sender("Jay Gatsby", "199288333", "US", "New York", "Unknown", None, "jay@gatsby.com"),
          "green light"
        )

        t.isDefined === true
        val uuid = t.get.id
        val headers = FakeHeaders(Seq("ACCEPT" -> Seq("application/json")))

        val url = "/transactions/" + uuid.toString
        var page = route(FakeRequest(GET, url)).get
        status(page) must equalTo(OK)
        contentType(page) must beSome.which(_ == "text/html")

        page = route(FakeRequest(GET, url, headers, "")).get
        status(page) must equalTo(OK)
        contentType(page) must beSome.which(_ == "application/json")
      }
    }

    "updates the resource" in {
      running(FakeApplication()) {
        val t = Transaction.create(
          10,
          Receiver("Amir Sookhnanan", "18281300200", "IN"),
          Sender("Sita Ramkewan", "128319283891", "SR", "Paramaribo", "Unknown", None, "prettysita@example."),
          "vishnu"
        )

        t.isDefined === true
        val uuid = t.get.id

        val url = "/transactions/" + uuid.toString
        val body = "sender_name=Jack+Black&amount=29399"
        var page = route(FakeRequest(GET, url, FakeHeaders(), body)).get
        status(page) must equalTo(OK)

        val transaction = Transaction.findById(t.get.id)
        transaction.isDefined === true
        transaction.get.sender.name === "Jack Black"
        transaction.get.amount === 29299
      }
    }

    "create a new resource" in {
      running(FakeApplication()) {
        val count = Transaction.count

        val url = "/transactions"
        val body = "amount=4000&payment=1&secret=Winer92&receiver_name=Q&receiver_mobile=123&receiver_country=XX&sender_name=Z&sender_address=Nothing+32&sender_phonenumber=5550&sender_email=blank%40example.com&sender_city=NYC&sender_country=US"
        val params = Map(
          "amount"             -> Seq("4000"),
          "payment"            -> Seq("1"),
          "secret"             -> Seq("Winer92"),
          "receiver_name"      -> Seq("Q"),
          "receiver_mobile"    -> Seq("1234"),
          "receiver_country"   -> Seq("XX"),
          "sender_name"        -> Seq("Z"),
          "sender_address"     -> Seq("Nothing 32"),
          "sender_phonenumber" -> Seq("5550"),
          "sender_email"       -> Seq("blank@example.com"),
          "sender_city"        -> Seq("NYC"),
          "sender_country"     -> Seq("US")
        )
        //var page = route(FakeRequest(POST, url, FakeHeaders(), body)).get
        var page = route(FakeRequest(POST, url).withRawBody(body.getBytes)).get

        val form = Form(
          mapping(
            "amount" -> number,
            "payment" -> number,
            "secret" -> nonEmptyText,
            "sender_name" -> nonEmptyText,
            "sender_address" -> nonEmptyText,
            "sender_city" -> nonEmptyText,
            "sender_country" -> nonEmptyText,
            "receiver_name" -> nonEmptyText, 
            "receiver_mobile" -> nonEmptyText,
            "receiver_country" -> nonEmptyText
          )(TransactionForm.apply)(TransactionForm.unapply)
        )

        //form.bind(params).hasErrors must beFalse
        //form.errors.size must equalTo(0)

        /*val result = route(FakeRequest(POST, url), params).get
        contentAsString(page) must contain("blank@example.com")
        status(page) must equalTo(OK)*/

        Transaction.count === count+1
      }
    }
    /*"return details upon request" in {
      running(FakeApplication()) {
        pending
      }
    }*/
  }
}
