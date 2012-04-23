package org.codefirst.katze.core.scm

import org.eclipse.jgit.storage.file._
import org.eclipse.jgit.api._
import org.eclipse.jgit.api.{Git => JGit}
import org.eclipse.jgit.errors._
import org.eclipse.jgit._
import scala.collection.JavaConverters._

import org.codefirst.katze.core.{ID,Ticket}
import sjson.json.Primitives

class Git(val url : String) extends Scm with Primitives {
  type T = Int

  def init =
    0

  def fetch(state : T) =
    state

  def format =
    IntFormat

  lazy val repos = {
    val builder = new FileRepositoryBuilder()
    val file =new java.io.File(url)

    builder.findGitDir(file).
      readEnvironment().
      findGitDir().
      build()
  }

  def logs =
    try {
      val git = new JGit(repos)
      git.log().call().asScala.map { c =>
        Commit(
          c.getId.toString,
          c.getAuthorIdent.getName,
          c.getFullMessage)
      }
    } catch {
      case _ => Seq()
    }

  def commits(state : T, id : ID[Ticket]) =
    logs.filter { _.message contains id.value }
}
