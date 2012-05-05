package org.codefirst.katze.core.scm

import org.codefirst.katze.core.{ID,Ticket}
import java.util.Date

case class Commit(
  id : String,
  author  : String,
  message : String,
  createdAt : Date = new Date()
) {
  def isNowCommit : Boolean =
    message contains "[from now]"
}

object Commit {
  case class NowCommit(from : Date, to : Date, count : Int)

  def uniq(xs : List[Commit]) : List[Either[NowCommit, Commit]]= {
    xs match {
      case Nil =>
        Nil
      case List(y) if y.isNowCommit =>
        List( Left(NowCommit(y.createdAt, y.createdAt, 1)) )
      case List(y) =>
        List( Right(y) )
      case y :: ys if y.isNowCommit =>
        uniq(ys) match {
          case Left(n) :: zs =>
            Left(n.copy(count = n.count +1,
                        to    = y.createdAt)) :: zs
          case zs =>
            Left(NowCommit(y.createdAt, y.createdAt, 1)) :: zs
        }
      case y :: ys =>
        Right(y) :: uniq(ys)
    }
  }
}

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
