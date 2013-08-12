import anorm._
import anorm.SqlParser._

import org.specs2.mutable._

import java.awt.Point
import java.util.{ UUID }

import models._

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
    "be retrieved by id" in { 
      running(FakeApplication()) {
        DB.withConnection { implicit connection =>
          def insert(street: String): List[UUID] = {
            SQL("""INSERT INTO cash_points
            (location, address, city, country, serial, token, note)
            values (
              POINT(23, 43),
              {street},
              'Metropolis',
              'US',
              '1231-28AA-BSI1-23SK-KS18-12JJ',
              'jajajajajaj',
              'nothing to say'
            )""").on('street -> street).executeInsert(parser *)
          }
          val new_id = insert("Somestreet 123343").head
          CashPoint.findById(new_id).isDefined === true
          CashPoint.findById(new_id).foreach(cash_point =>
            cash_point.location.city === "Metropolis"
          )
        }
      }
    }
    "be nothing upon fetching a non-existent UUID" in { 
      running(FakeApplication()) {
        Transaction.findById(new UUID(0, 0)) === None
      }
    }
    "be instantiable" in {
      val iut = CashPoint (
        UUID.randomUUID,
        Location(new Point(23, 443), "Long Island Str 23", "Gotham", "US"),
        "JJQI-293A-2JJ2-JIQ5-823N",
        "blablablablah",
        true,
        None
      )
      iut.location.address === "Long Island Str 23"
      iut.location.city    === "Gotham"
      iut.location.country === "US"
      iut.note             === None
    }
    "be creatable with helper" in { 
      running(FakeApplication()) {
        val p = CashPoint.create(
          "UC_NYC_MANH_WALLSTR_0012",
          Location(new Point(232, 433), "Wall Street 12", "NYC", "US"),
          Some("The ATM near the NYSE at 11 Wall Street")
        )
        p.isDefined === true
        p.get.serial === "UC_NYC_MANH_WALLSTR_0012"
        p.get.location.country === "US"
      }
    }
  }
}
