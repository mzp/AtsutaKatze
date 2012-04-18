package org.codefirst.katze.core.scm

import org.codefirst.katze.core.{ID,Ticket}

case class Commit(
  id : String,
  author  : String,
  message : String
)

trait Scm {
  def url : String
  def commits(id : ID[Ticket]) : Iterable[Commit]
}
