package models

import anorm._
import anorm.SqlParser._

import java.awt.Point
import java.sql.Timestamp
import java.util.{ Date, UUID }
import org.postgresql.geometric.PGpoint
import play.api.db.DB

trait Extractable {
  implicit def rowToUUID: Column[UUID] = {
    Column.nonNull[UUID] { (value, meta) =>
      value match {
        case uuid: UUID     => Right(uuid)
        case string: String => Right(UUID.fromString(string))
        case _ => Right(new UUID(0, 0))
      }
    }
  }

  implicit def rowToDate: Column[Date] = {
    Column.nonNull[Date] { (value, meta) => 
      value match {
        case string: String => Right(new Date(Timestamp.valueOf(string).getTime))
        case stamp: Timestamp => Right(new Date(stamp.getTime()))
        case _ => Right(new Date(0L))
      }
    }
  }

  implicit def rowToBigDecimal: Column[BigDecimal] = {
    Column.nonNull[BigDecimal] { (value, meta) => 
      value match {
        case value:Double => Right(BigDecimal.double2bigDecimal(value))
        case value:Long   => Right(BigDecimal.long2bigDecimal(value))
        case value:java.math.BigDecimal => Right(new BigDecimal(value))
        case _            => Right(BigDecimal.int2bigDecimal(0))
      }
    }
  }

  implicit def rowToPoint: Column[Point] = {
    Column.nonNull[Point] { (value, meta) =>
      value match {
        case point: PGpoint => {
          val p = new Point
          p.setLocation(point.x, point.y)
          Right(p)
        }
        case point: Point   => Right(new Point(point))
        case _              => Right(new Point(0, 0))
      }
    }
  }
}

// TODO: extract findBy methods to Findable

class Model extends Extractable
