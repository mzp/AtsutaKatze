package org.codefirst.katze.core

case class Project(tickets : List[Ticket])

object Project {
  def dummy : Project =
    Project(List(Ticket.make("foo1", Open),
                 Ticket.make("foo2", Close),
                 Ticket.make("foo3", Open)))

  def empty =
    Project(List())
}

