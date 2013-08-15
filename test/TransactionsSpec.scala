package test

import org.specs2.mutable._

import java.util.UUID
import play.api.test._
import play.api.test.Helpers._

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
class TransactionsSpec extends Specification {
  
  "/transactions" should {
    
    "send 404 on a bad request" in {
      running(FakeApplication()) {
        route(FakeRequest(GET, "/transactions")) must beNone        
      }
    }
    
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
        route(request) must beNone

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
        route(request) must beNone

        val page = route(request).get
        contentType(page) must beSome.which(_ == "application/json")
      }
    }

    /*"return details upon request" in {
      running(FakeApplication()) {
        pending
      }
    }*/
  }
}
