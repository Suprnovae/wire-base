package test.controllers

import anorm._
import models._
import org.apache.commons.codec.binary.Base64
import org.specs2.mutable._
import play.api.db.DB
import play.api.Play.current
import play.api.test._
import play.api.test.Helpers._

class BaseSpecification extends Specification {
  val base64token = new String(Base64.encodeBase64("wire:wow!".getBytes))
  val headers = FakeHeaders(Seq(
    "AUTHORIZATION" -> Seq("Basic " + base64token)
  ))

  object empty_set extends Before {
    def before {
      running(FakeApplication()) {
        DB.withConnection { implicit c =>
          SQL("""DELETE FROM withdrawals CASCADE""").executeUpdate()
          SQL("""DELETE FROM transactions CASCADE""").executeUpdate()
          SQL("""DELETE FROM cash_points CASCADE""").executeUpdate()
        }
      }
    }
  }
}
