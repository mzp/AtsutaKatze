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

    def execute(store : Store) {
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

    def execute( store : Store) {
      val ticket = Ticket.make(subject,Open)
      store.apply(Patch.make(AddAction(ticket)))
    }
  } }

  define("changes") { new Command {
    val description =
      "show changes"

    def execute( store : Store) {
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

    def execute( store : Store) {
      for( t <- targets.asScala ) {
        val dest = new Store(new File(t))
        Store.copy(store, dest)
      }
    }
  } }

  define("pull") { new Command {
    val description =
      "get remote changes"

    @Parameter(description = "")
    var targets : java.util.List[String] = null

    def execute( store : Store) {
      for( t <- targets.asScala.headOption ) {
        val dest = new Store(new File(t))
        Store.copy(dest, store)
      }
    }
  } }
}


