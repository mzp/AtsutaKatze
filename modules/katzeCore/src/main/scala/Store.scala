package org.codefirst.katze.core

import java.util.Date
import java.io.{File, FileInputStream, FileOutputStream}
import sjson.json._
import dispatch.json._

case class Patch(id : ID, action : Action, createdAt : Date)
object Patch {
  def make(action : Action) =
    Patch(ID.get, action, new Date)
}

sealed abstract class Action {
  def apply(project : Project) : Project
}
case class AddAction(
  ticket : Ticket
) extends Action {
  def apply(project : Project) =
    project.copy(tickets = ticket :: project.tickets)
}

class Store(root : File) {
  import JsonSerialization._
  import KatzeProtocol._

  root.mkdirs

  def open_in(file : File) =
    new FileInputStream(file)

  def open_out(file : File) =
    new FileOutputStream(file)

  def file(name : String) =
    tee(new File( root, name )) { file =>
      file.getParentFile.mkdirs
    }

  // TODO: need file lock
  def current : Project =
    sure {
      using( open_in( file("current") ) ) { is =>
        val json = JsValue.fromStream(is)
        fromjson[Project](json)
      }
    } getOrElse {
      Project.empty
    }

  // TODO: need file lock
  def apply(patch : Patch) {
    val project = patch.action.apply(current)
    using( open_out( file("current") )) { os =>
      os.write(tojson(project).toString.getBytes)
    }

    using( open_out( file("patches/%s".format(patch.id.value)) )) { os =>
      os.write(tojson(patch).toString.getBytes)
    }
  }
}
