import anorm._
import play.api.db.DB
import java.util.UUID

case class Receiver(
  name: String,
  phonenumber: String,
  country: String
)

case class Sender(
  name: String,
  phonenumber: String,
  country: String,
  city: String,
  address: String,
  postalcode: Option[String],
  email: String
)

case class Transaction(
  id: String,
  amount: Double,
  receiver: Receiver,
  sender: Sender
)

object Transaction {
  def all(): List[Transaction] = List[Transaction]()
  def findById(id: Any): Option[Transaction] = id match {
    case id: UUID => None
    case id: String => None
    case _ => None
  }
  def findByTokens(code: String, secret: String): Option[Transaction] = None
  def complete() {}
  //def save(): Transaction = {}
}

// find helper
// find by transaction code and token
// find by id
// fields 
// save helper
// destroy helper
