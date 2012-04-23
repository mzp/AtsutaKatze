package org.codefirst.katze.cli

import java.io.File
import com.beust.jcommander._
import org.codefirst.katze.core._

object DefaultCommands extends CommandDefinition {
  import scala.collection.JavaConverters._

  define("list") { new Command {
    val description =
      "show all tickets"

    val status : Map[Status, String] =
      Map(Open  -> " ",
          Close -> "x" )

    def execute(repos : Repository) {
      for(t <- repos.tickets) {
        printf("%s %s %s\n",
               t.id.value,
               status(t.status),
               t.subject)
      }
    }
  } }

  define("add") { new Command {
    val description =
      "add ticket"

    @Parameter(names = Array("-s"), description = "subject")
    var subject : String = ""

    def execute( repos : Repository) {
      val ticket = Ticket.make(subject,Open)
      repos.apply(Patch.make(AddAction(ticket)))
    }
  } }

  define("mv") { new Command {
    val description =
      "rename ticket"

    @Parameter(names = Array("-s"), description = "subject")
    var subject : String = ""

    @Parameter(description = "")
    var ids : java.util.List[String] = null

    def execute(repos : Repository) {
      for(id <- ids.asScala) {
        repos.ticket(id) match {
          case Left(str) =>
            println(str)
          case Right(t) =>
            repos.apply(Patch.make(UpdateAction.subject(t, subject)))
        }
      }
    }
  } }

  define("rm") { new Command {
    val description =
      "remove ticket"

    @Parameter(description = "")
    var ids : java.util.List[String] = null

    def execute(repos : Repository) {
      for(id <- ids.asScala) {
        repos.ticket(id) match {
          case Left(str) =>
            println(str)
          case Right(t) =>
            repos.apply(Patch.make(DeleteAction(t)))
        }
      }
    }
  } }

  define("changes") { new Command {
    val description =
      "show changes"

    def execute( repos : Repository) {
      for(p <- repos.changes) {
        printf("%s %s\n", p.id.short, p.action.summary)
      }
    }
  } }

  define("push") { new Command {
    val description =
      "push local changes"

    @Parameter(description = "")
    var targets : java.util.List[String] = null

    def execute( repos : Repository) {
      for( t <- targets.asScala ) {
        val dest = Repository.open(t)
        Repository.copy(repos, dest)
      }
    }
  } }

  define("pull") { new Command {
    val description =
      "get remote changes"

    @Parameter(description = "")
    var targets : java.util.List[String] = null

    def execute( repos : Repository) {
      for( t <- targets.asScala.headOption ) {
        val dest = Repository.open(t)
        Repository.copy(dest, repos)
      }
    }
  } }

  define("scm") { new Command {
    val description =
      "set repository"

    @Parameter(description = "")
    var repositories : java.util.List[String] = null

    def execute( repos : Repository) {
      Option(repositories).flatMap(_.asScala.headOption) match {
        case None =>
          val scm =
             repos.config.scm getOrElse { "none" }
          println(scm)
        case Some(url) =>
          // write mode
          repos.updateConfig {
            _.copy(scm = Some(url))
          }
      }
    }
  } }

  define("commits") { new Command {
    val description =
      "show related commits"

    @Parameter(description = "")
    var tickets : java.util.List[String] = null

    def either[A,B](x : Either[A,B]) =
      x match {
        case Right(x) => Some(x)
        case Left(_)  => None
      }

    def execute( repository : Repository) {
      val commits = for {
        id     <- Option(tickets).flatMap(_.asScala.headOption)
        ticket <- either(repository.ticket(id))
      } yield repository.commits(ticket)
      commits getOrElse { List() } foreach { case c =>
        printf("[%s] %s\n%s\n", c.id, c.author, c.message)
      }
    }
  } }

}
