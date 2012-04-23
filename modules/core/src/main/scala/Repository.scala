package org.codefirst.katze.core

import store._
import java.util.Date
import scala.collection.mutable.ListBuffer
import java.io.File
import org.codefirst.katze.core.scm._
import sjson.json.{Reads,Writes, JsonSerialization}

class Repository(store : Store) {
  import JsonSerialization._
  import KatzeProtocol._

  private def read[T](name : String)(implicit fjs : Reads[T]) : Option[T] =
    store.read(name).map(fromjson[T](_)(fjs))

  private def write[T](name : String, obj : T)(implicit fjs : Writes[T]) : Unit =
    store.write(name, tojson(obj)(fjs))

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

  def ticket(id : String) : Either[String, Ticket] = {
    val xs =
      tickets.filter(_.id.value.startsWith(id))
    xs match {
      case List() =>
        Left("not found ticket")
      case List(t) =>
        Right(t)
      case _ =>
        Left("Too many ticket")
    }
  }

  def tickets : List[Ticket] = {
    read[List[Ticket]]("current") getOrElse {
      List()
    }
  }

  // TODO: need file lock
  def apply(patch : Patch) {
    val next = head match {
      case Some(p) =>
        patch.copy(depends = Some(p.id))
      case None =>
        patch
    }

    write("current", next.action(tickets))
    write("patches/%s".format(next.id.value), next)
    write("head", next.id)
  }

  def config : Config = {
    read[Config]("config") getOrElse {
      Config.empty
    }
  }

  def updateConfig(f : Config => Config) {
    write("config", f(this.config))
  }

  type SCM = {
    def fetch : Unit
    def commits(t : Ticket) : Iterable[Commit]
  }

  def scm : SCM = {
    config.scm map { url =>
      (new Object {
        val scm = Scm(url)

        def state : scm.T =
          read[scm.T]("fetch/%s".format(url))(scm.format) getOrElse {
            scm.init
          }

        def fetch {
          val next =
            scm.fetch(state)

          write("fetch/%s".format(url), next)(scm.format)
        }

        def commits(t : Ticket) : Iterable[Commit] =
          scm.commits(state, t.id)
      } : SCM)
    } getOrElse {
      new Object {
        def fetch {}
        def commits(t : Ticket) = List()
      }
    }
  }

  def fetch =
    scm.fetch

  def commits(t : Ticket) : Iterable[Commit] = {
    scm.commits(t)
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

  def open(uri : String) = {
    val x = new java.net.URI(uri)
    val store = x.getScheme match {
      case "file" =>
        new LocalStore(new File(x.getPath))
      case "http" =>
        new HttpStore(x)
    }
    new Repository(store)
  }
}
