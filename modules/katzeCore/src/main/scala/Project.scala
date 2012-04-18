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
