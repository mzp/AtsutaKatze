package org.codefirst.katze.cli

import java.io.File
import com.beust.jcommander._
import org.codefirst.katze.core.Store

object CommandMain {}

object Cli extends App {
  import scala.collection.JavaConverters._

  override def main(args : Array[String]) = {
    val store =
      new Store(new File(".katze"))

    val jc =
      new KatzeCommander(CommandMain)

    DefaultCommands(jc)

    jc.parse(args : _*)

    val name = jc.getParsedCommand()

    if(name == null) {
      jc.usage()
    } else {
      jc.getCommands.asScala.get(name) match {
        case Some(command) =>
          for(obj <- command.getObjects.asScala)
            obj.asInstanceOf[Command].execute(store)
        case None =>
          jc.usage
      }
    }
  }
}
