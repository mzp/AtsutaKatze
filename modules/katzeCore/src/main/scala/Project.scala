package org.codefirst.katze.core

case class Project(
  id      : ID[Project],
  tickets : List[Ticket]
)

object Project {
  def empty =
    Project(ID("0"),List())
}

// non sharable conifg
case class ProjectConfig(
  repository : Option[String]
)

object ProjectConfig {
  def empty =
    ProjectConfig(None)
}
