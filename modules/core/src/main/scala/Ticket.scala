package org.codefirst.katze.core

sealed abstract class Status
case object Open  extends Status
case object Close extends Status

object Status {
  def fromString(str : String) =
    Map("open" -> Open,
        "close" -> Close) {
      str.toLowerCase
    }
}

case class Ticket(
  id      : ID[Ticket],
  subject : String,
  status  : Status
)

object Ticket {
  def make(subject : String, status : Status) =
    Ticket(ID.get, subject, status)
}
