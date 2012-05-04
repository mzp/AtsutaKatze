package org.codefirst.katze.core.scm

import org.codefirst.katze.core.{ID,Ticket}
import java.util.Date

case class Commit(
  id : String,
  author  : String,
  message : String,
  createdAt : Date = new Date()
)

trait Scm {
  type T
  def init  : T
  def fetch(state : T) : T
  def commits(state :T, id : ID[Ticket]) : Iterable[Commit]
  def format : sjson.json.Format[T]
}

object Scm {
  def apply(url : String) : Scm =
    url match {
      case s if s.startsWith("https://github.com") =>
        new GitHub(s)
      case _ =>
        new Git(url)
    }
}
