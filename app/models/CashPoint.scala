package models

import anorm._
import anorm._
import java.sql.Timestamp
import java.util.{ Date, UUID }

case class Location(
  coordinates: String,
  address: String,
  city: String,
  country: String
)

case class CashPoint(
  id: UUID,
  location: Location,
  serial: String,
  tokenHash: String,
  active: Boolean
)

object CashPoint {
  implicit def rowToUUID: Column[UUID] = {
    Column.nonNull[UUID] { (value, meta) =>
      value match {
        case uuid: UUID     => Right(uuid)
        case string: String => Right(UUID.fromString(string))
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

  def findAll(): Seq[CashPoint] = List[CashPoint]()
  def findById(id: UUID): Option[Transaction] = None
  //def findNear(
  //def validate(id: UUID, token: String, transaction: Transaction)
}
