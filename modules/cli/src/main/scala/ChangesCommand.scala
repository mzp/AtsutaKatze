package org.codefirst.katze.cli
import org.codefirst.katze.core._
import com.beust.jcommander._
import scala.collection.JavaConverters._
import Console._
import java.util.Date

object ChangesCommand {
  def apply(definition : CommandDefinition) {
    definition.withRepos("changes", "show changes")(new Object{}) { (repos, _)  =>
      for(p <- repos.changes) {
        printf(YELLOW + "patch %s\n" + RESET, p.id.value)
        printf("Date: %s\n\n", p.createdAt)
        println("    " + p.action.summary)
        println("")
        for((field, value) <- p.action.format)
          println("    %-7s: %s".format(field, value))
        println("")
      }
    }
  }
}

