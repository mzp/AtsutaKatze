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

    def execute(store : Repository) {
      for(t <- store.current.tickets) {
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

    def execute( store : Repository) {
      val ticket = Ticket.make(subject,Open)
      store.apply(Patch.make(AddAction(ticket)))
    }
  } }

  define("mv") { new Command {
    val description =
      "rename ticket"

    @Parameter(names = Array("-s"), description = "subject")
    var subject : String = ""

    @Parameter(description = "")
    var ids : java.util.List[String] = null

    def execute(store : Repository) {
      for(id <- ids.asScala) {
        store.ticket(id) match {
          case Left(str) =>
            println(str)
          case Right(t) =>
            store.apply(Patch.make(UpdateAction.subject(t, subject)))
        }
      }
    }
  } }

  define("rm") { new Command {
    val description =
      "remove ticket"

    @Parameter(description = "")
    var ids : java.util.List[String] = null

    def execute(store : Repository) {
      for(id <- ids.asScala) {
        store.ticket(id) match {
          case Left(str) =>
            println(str)
          case Right(t) =>
            store.apply(Patch.make(DeleteAction(t)))
        }
      }
    }
  } }

  define("changes") { new Command {
    val description =
      "show changes"

    def execute( store : Repository) {
      for(p <- store.changes) {
        printf("%s %s\n", p.id.short, p.action.summary)
      }
    }
  } }

  define("push") { new Command {
    val description =
      "push local changes"

    @Parameter(description = "")
    var targets : java.util.List[String] = null

    def execute( store : Repository) {
      for( t <- targets.asScala ) {
        val dest = Repository.open(t)
        Repository.copy(store, dest)
      }
    }
  } }

  define("pull") { new Command {
    val description =
      "get remote changes"

    @Parameter(description = "")
    var targets : java.util.List[String] = null

    def execute( store : Repository) {
      for( t <- targets.asScala.headOption ) {
        val dest = Repository.open(t)
        Repository.copy(dest, store)
      }
    }
  } }

  define("scm") { new Command {
    val description =
      "set repository"

    @Parameter(description = "")
    var repositories : java.util.List[String] = null

    def execute( repository : Repository) {
      Option(repositories).flatMap(_.asScala.headOption) match {
        case None =>
          // read mode
          val s =
            repository.config(repository.current).scm getOrElse { "<none>" }
          println(s)
        case Some(url) =>
          repository.updateConfig(repository.current) {
            _.copy(scm = Some(url))
          }
      }
    }
  } }
}
