package org.codefirst.katze.core

import org.codefirst.katze.core.scm._

case class Project(
  id      : ID[Project],
  tickets : List[Ticket]
) {
  def scm(repository : Repository) =
    repository.config(this).scm map {
      new Git(_)
    }

  def commits(repository : Repository, ticket : Ticket) : Iterable[Commit] =
    scm(repository) map {
      _.commits(ticket.id)
    } getOrElse {
      Seq()
    }
}

object Project {
  def empty =
    Project(ID("0"),List())
}

// non sharable conifg
case class ProjectConfig(
  scm: Option[String]
)

object ProjectConfig {
  def empty =
    ProjectConfig(None)
}
