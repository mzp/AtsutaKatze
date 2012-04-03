package org.codefirst.katze.core

import store.Store
import java.util.Date
import scala.collection.mutable.ListBuffer
import store.LocalStore
import java.io.File

class Repository(store : Store) {
  import KatzeProtocol._

  def head : Option[Patch] =
    store.read[ID[Patch]]("head").flatMap(patch(_))

  def patch(id : ID[Patch]) : Option[Patch] =
    store.read[Patch]("patches/%s".format(id.value))

  def changes : List[Patch] =
    head.map(changes(_)) getOrElse { List() }

  private def changes(patch : Patch) : List[Patch] = {
    val buffer : ListBuffer[Patch] = new ListBuffer
    var p : Option[Patch] = Some(patch)
    while(p != None){
      buffer += (p.get)
      p = p.get.depends.flatMap(this.patch(_))
    }
    buffer.toList
  }

  def findTicket(id : String) : Either[String, Ticket] = {
    val xs =
      current.tickets.filter(_.id.value.startsWith(id))
    xs match {
      case List() =>
        Left("not found ticket")
      case List(t) =>
        Right(t)
      case _ =>
        Left("Too many ticket")
    }
  }

  // TODO: need file lock
  def current : Project =
    store.read[Project]("current") getOrElse {
      Project.empty
    }

  // TODO: need file lock
  def apply(patch : Patch) {
    val next = head match {
      case Some(p) =>
        patch.copy(depends = Some(p.id))
      case None =>
        patch
    }
    val project = next.action(current)

    store.write("current", project)
    store.write("patches/%s".format(next.id.value), next)
    store.write("head", next.id)
  }
}

object Repository {
  def isPushable(from : Repository, to : Repository) : Boolean =
    to.head match {
      case Some(head) =>
        from.changes.contains(head)
      case None =>
        true
    }

  def copy(from : Repository, to : Repository) {
    if( ! isPushable(from, to) )
      throw new RuntimeException("not fast foward")

    // TODO: more effective!
    for( patch <- (from.changes diff to.changes).reverse ) {
      to.apply(patch)
    }
  }

  def local(path : String) =
    new Repository(new LocalStore(new File(path)))
}
