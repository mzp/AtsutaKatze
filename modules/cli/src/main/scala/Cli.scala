package org.codefirst.katze.cli

import java.io.File
import com.beust.jcommander._
import org.codefirst.katze.core.Repository

object CommandMain {
  @Parameter(names = Array("-d","--katze-dir"), description = "katze home")
  val katze_dir : String = null
}

object Cli extends App {
  import scala.collection.JavaConverters._

  def findKatze(dir : File) : Option[String] = {
    val katze =
      new File(dir, ".katze")
    if( katze.exists )
      Some(katze.getPath)
    else {
      Option(dir.getParentFile) flatMap {
        findKatze(_)
      }
    }
  }

  override def main(args : Array[String]) = {
    val jc =
      new KatzeCommander(CommandMain, DefaultCommands)
    jc.parse(args : _*)

    val name = jc.getParsedCommand()

    val repository : Option[Repository] =
      Option(CommandMain.katze_dir) orElse {
        findKatze(new File(".").getAbsoluteFile)
      } map { path =>
        Repository.local( path )
      }

    if(name == null) {
      jc.usage()
    } else {
      val command =
        DefaultCommands.commands(name)
      command.action(repository, command.params)
    }
  }
}
