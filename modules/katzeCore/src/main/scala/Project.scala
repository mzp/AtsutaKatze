package org.codefirst.katze.core

case class Project(tickets : List[Ticket])

object Project {
  def empty =
    Project(List())
}

