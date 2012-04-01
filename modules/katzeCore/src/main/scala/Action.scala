package org.codefirst.katze.core

sealed abstract class Action {
  def apply(project : Project) : Project
  def summary = toString
}

case class AddAction(
  ticket : Ticket
) extends Action {
  def apply(project : Project) =
    project.copy(tickets = ticket :: project.tickets)

  override def summary =
    "[addTicket]%s".format(ticket.subject)
}

case class DeleteAction(
  ticket : Ticket
) extends Action {
  def apply(project : Project) =
    project.copy(tickets = project.tickets.filterNot(_.id == ticket.id))

  override def summary =
    "[delTicket]%s".format(ticket.subject)
}
