import anorm._
import anorm.SqlParser._

import org.specs2.mutable._

import java.util.{ UUID }

import play.api.db.DB
import play.api.Play.current
import play.api.test._
import play.api.test.Helpers._

import scala.util.{ Random, Try }

class CashPointSpec extends Specification {
  step {
    running(FakeApplication()) {
      DB.withConnection { implicit connection => 
        SQL("""DELETE FROM cash_points""").executeUpdate()
      }
    }
  }
  implicit def rowToUUID: Column[UUID] = {
    Column.nonNull[UUID] { (value, meta) =>
      value match {
        case uuid: UUID     => Right(uuid)
        case string: String => Right(UUID.fromString(string))
      }
    }
  }
  val parser = { get[UUID]("id") }
  "CashPoint" should {
    "be retrieved by id" in { pending }
    "be nothing upon fetching a non-existent UUID" in { pending }
    "be instantiable" in { pending }
    "be creatable with helper" in { pending }
  }
}
