package org.codefirst.katze.cli

import java.io.File
import com.beust.jcommander._
import org.codefirst.katze.core._

trait Command {
  def execute(store : Store) : Unit
}

@Parameters(separators = "=", commandDescription = "List all tickets")
class CommandList extends Command {
  def execute(store : Store) {
    for(t <- store.current.tickets) {
      println("%s... %s %s".format(t.id.short,
                                   status(t.status),
                                   t.subject))
    }
  }

  def status(s : Status) = {
    val map : Map[Status, String] =
      Map(Open  -> " ",
          Close -> "x" )
    map(s)
  }
}

@Parameters(separators = "=", commandDescription = "Add ticket")
class CommandAdd extends Command {
  @Parameter(names = Array("-s"), description = "subject")
  var subject : String = ""

  def execute( store : Store) {
    val ticket = Ticket.make(subject, Open)
    store.apply(Patch.make(AddAction(ticket)))
  }
}

@Parameters(separators = "=", commandDescription = "Add ticket")
class CommandMain {
}

object KatzeCli extends App {
  import scala.collection.JavaConverters._

  override def main(args : Array[String]) = {
    val cm = new CommandMain

    // hack for ambigious constructor
    val jc = new JCommander(cm, (null : java.util.ResourceBundle ))

    jc.addCommand("list",new CommandList())
    jc.addCommand("add", new CommandAdd())

    jc.parse(args : _*)

    val name = jc.getParsedCommand()

    val store = new Store(new File(".katze"))
    for( obj <- jc.getCommands.asScala(name).getObjects.asScala)
      obj.asInstanceOf[Command].execute(store)
  }
}