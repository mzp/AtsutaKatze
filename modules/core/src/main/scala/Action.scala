package org.codefirst.katze.core

sealed abstract class Action {
  def apply(tickets : List[Ticket]) : List[Ticket]
  def summary = toString
}

case class AddAction(
  ticket : Ticket
) extends Action {
  def apply(tickets : List[Ticket]) =
    ticket :: tickets

  override def summary =
    "[addTicket]%s".format(ticket.subject)
}

case class UpdateAction(
  from : Ticket,
  to   : Ticket
) extends Action {
  assert(from.id == to.id)

  def apply(tickets : List[Ticket]) =
    tickets.map{ t =>
      if( t.id == from.id )
        to
      else
        t
    }

  override def summary =
    "[updateicket]%s -> %s".format(from.subject, to.subject)
}
object UpdateAction {
  def subject(t : Ticket, subject : String) =
    UpdateAction(t, t.copy(subject = subject))
}


case class DeleteAction(
  ticket : Ticket
) extends Action {
  def apply(tickets : List[Ticket]) =
    tickets.filterNot(_.id == ticket.id)

  override def summary =
    "[delTicket]%s".format(ticket.subject)
}