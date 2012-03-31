package org.codefirst.katze.core

import sjson.json._

sealed abstract class Status
case object Open  extends Status
case object Close extends Status

case class Ticket(
  uuid    : String,
  subject : String,
  status  : Status
)

object Ticket {
  def make(subject : String, status : Status) =
    Ticket(ID.get,
           subject,
           status)
}

object TicketProtocol extends DefaultProtocol {
  import JsonSerialization._

  implicit val StatusFormat : Format[Status] =
    wrap("status")( _.toString,
                   ( _ : String).toLowerCase match {
                     case "open" => Open
                     case "close" => Close
                   })

  implicit val TicketFormat : Format[Ticket] =
    asProduct3("uuid", "subject", "status")(Ticket.apply _)(Ticket.unapply(_).get)
}
