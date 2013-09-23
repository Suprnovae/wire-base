package models

import anorm._
import anorm.SqlParser._
import com.github.t3hnar.bcrypt._
import java.awt.geom.Point2D
import java.sql.Timestamp
import java.util.{ Date, UUID }
import org.postgresql.geometric.PGpoint
import play.api.db.DB
import play.api.Play.current

case class Location(
  coordinates: Point2D,
  address: String,
  city: String,
  country: String
)

case class CashPointForm(
  serial: String,
  note: String,
  address: String,
  city: String,
  country: String,
  longitude: Float,
  latitude: Float 
)

case class CashPoint(
  id: UUID,
  location: Location,
  serial: String,
  tokenHash: String,
  active: Boolean,
  note: Option[String]
)

object CashPoint extends Model {
  val complete = {
    get[UUID]("cash_points.id")~
    get[Point2D]("cash_points.location")~
    get[String]("cash_points.address")~
    get[String]("cash_points.city")~
    get[String]("cash_points.country")~
    get[String]("cash_points.serial")~
    get[String]("cash_points.token")~
    get[Option[String]]("cash_points.note")~
    get[Boolean]("cash_points.active")~
    get[Date]("cash_points.created_at") map {
      case id~loc~addr~city~country~serial~token~note~active~created_at =>
      CashPoint(
        id,
        Location(loc, addr, city, country),
        serial,
        token,
        active,
        note
      )
    }
  }
  val simple = complete

  def create(
    serial: String, 
    location: Location, 
    note: Option[String]
  ): Option[CashPoint] = {
    DB.withConnection { implicit connection => 
      val res = SQL("""
        INSERT INTO cash_points (
          location,
          address,
          city,
          country,
          serial,
          token,
          note,
          active
        ) values (
          {coord},
          {address},
          {city},
          {country},
          {serial},
          {token},
          {note},
          FALSE
        )"""
      ).on(
        'coord                -> new PGpoint(
          location.coordinates.getX, 
          location.coordinates.getY),
        'address              -> location.address,
        'city                 -> location.city,
        'country              -> location.country,
        'serial               -> serial,
        'token                -> (serial+"we still need to think about this").bcrypt,
        'note                 -> note
      ).executeInsert[List[CashPoint]](CashPoint.simple *)
      CashPoint.findById(res.head.id)
    }
  }

  def findAll(): Seq[CashPoint] = {
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM cash_points").as(CashPoint.simple *)
    }
  }

  def findById(id: UUID): Option[CashPoint] = {
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM cash_points WHERE id = {id}")
      .on('id -> id).as(CashPoint.simple.singleOpt)
    }
  }

  def count: Long = {
    DB.withConnection { implicit c =>
      SQL("""SELECT COUNT(*) AS c FROM cash_points""")
        .apply().head[Long]("c")
    }
  }
  //def findNear(location: Point): Seq[CashPoint] = { }

  def validate(id: UUID, secret: String): Boolean = {
    CashPoint.findById(id) match {
      case Some(p) => if ((secret+"we still need to think about this").isBcrypted(p.tokenHash)) true else false
      case None    => false
    }
  }

  def modify(id: UUID, active: Boolean, note: String = null, location: Location = null): Option[CashPoint] = {
    DB.withConnection { implicit c =>
      val count = SQL("""
        UPDATE cash_points
        SET active = {active}
        WHERE id = {id} """)
        .on('id -> id, 'active -> active)
        .executeUpdate()
      return CashPoint.findById(id)
    }
  }
}
