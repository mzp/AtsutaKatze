package org.codefirst.katze.core.scm

import java.net.URI
import scala.collection.JavaConverters._

import dispatch.json._
import sjson.json._

import org.codefirst.katze.core._

class GitHub(val url : String,
             http : HttpExecutor = DefaultHttpExecutor) extends Scm {
  val uri = new URI(url)
  assert( uri.getHost == "github.com" )

  type T = List[Commit]

  def init =
    List()

  def format =
    KatzeProtocol.listFormat(KatzeProtocol.CommitFormat)


  val (user : String, repos : String) =
    uri.getPath.split("/").toList match {
      case _ :: user :: repos :: _ =>
        ( user, repos )
      case _ =>
        throw new RuntimeException("github url is invalid")
    }

  private def field(obj : JsValue, name : String) : JsValue =
    obj match {
      case JsObject(map) =>
        map(JsString(name))
      case _ =>
        throw new RuntimeException("invalid foramt")
    }

  private def str(obj : JsValue) =
    obj match {
      case JsString(x) => x
      case _ => obj.toString
    }

  def fetch(s : T) : T =
    http.get("https://api.github.com/repos/%s/%s/commits".format(user, repos)) { in =>
      val JsArray(commits) = JsValue.fromStream(in)
      commits map { obj =>
        val commit = field(obj, "commit")
        Commit(str(field(obj, "sha")),
               str(field(field(commit, "author"), "name")),
               str(field(commit, "message")))
      }
    }

  def commits(logs : T, id : ID[Ticket]) = {
    logs.filter { _.message contains id.value }
  }
}

