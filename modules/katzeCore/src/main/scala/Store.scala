package org.codefirst.katze.core

import java.util.Date
import java.io.{File, InputStream, FileInputStream, FileOutputStream}
import scala.collection.mutable.ListBuffer
import sjson.json._
import dispatch.json._

case class Patch(
  id        : ID[Patch],
  action    : Action,
  depends   : Option[ID[Patch]],
  createdAt : Date) {
}

object Patch {
  def make(action : Action) =
    Patch(ID.get, action, None, new Date)
}

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

class Store(root : File) {
  import JsonSerialization._
  import KatzeProtocol._

  root.mkdirs

  private def open_in(file : File) =
    new FileInputStream(file)

  private def open_out(file : File) =
    new FileOutputStream(file)

  private def file(name : String) =
    tee(new File( root, name )) { file =>
      file.getParentFile.mkdirs
    }

  private def read[T](name : String)(implicit fjs : Reads[T]) : Option[T] =
    sure {
      using( open_in( file(name) ) ) { is =>
        val json = JsValue.fromStream(is)
        fromjson[T](json)
      }
    }

  private def write[T](name : String, obj : T)(implicit fjs : Writes[T]) {
    using( open_out( file(name)) ) { os =>
      os.write(tojson(obj).toString.getBytes)
    }
  }

  def head : Option[Patch] =
    read[ID[Patch]]("head").flatMap(patch(_))

  def patch(id : ID[Patch]) : Option[Patch] =
    read[Patch]("patches/%s".format(id.value))

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

  // TODO: need file lock
  def current : Project =
    read[Project]("current") getOrElse {
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

    write("current", project)
    write("patches/%s".format(next.id.value), next)
    write("head", next.id)
  }
}

object Store {
  def isPushable(from : Store, to : Store) : Boolean =
    to.head match {
      case Some(head) =>
        from.changes.contains(head)
      case None =>
        true
    }

  def copy(from : Store, to : Store) {
    if( ! isPushable(from, to) )
      throw new RuntimeException("not fast foward")

    // TODO: more effective!
    for( patch <- (from.changes diff to.changes).reverse ) {
      to.apply(patch)
    }
  }
}
