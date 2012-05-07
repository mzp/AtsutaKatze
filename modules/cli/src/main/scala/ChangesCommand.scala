package org.codefirst.katze.cli
import org.codefirst.katze.core._
import com.beust.jcommander._
import scala.collection.JavaConverters._
import Console._
import java.util.Date

object ChangesCommand {
  def action(action : Action) : Seq[String] =
    action match {
      case AddAction(t) =>
        Seq(
          "id:      %s".format(t.id.value),
          "subject: %s".format(t.subject)
          )
      case DeleteAction(t) =>
        Seq(
          "id:      %s".format(t.id.value),
          "subject: %s".format(t.subject)
          )
      case UpdateAction(from, to) =>
        Seq(
          "id:      %s".format(from.id.value),
          if(from.subject == to.subject)
            "subject: %s".format(from.subject)
          else
            "subject: %s -> %s" .format(from.subject, to.subject),
          if(from.status == to.status)
            "status:  %s".format(from.status)
          else
            "status:  %s -> %s" .format(from.status, to.status)
        )
    }

  def apply(definition : CommandDefinition) {
    definition.withRepos("changes", "show changes")(new Object{}) { (repos, _)  =>
      for(p <- repos.changes) {
        printf(YELLOW + "patch %s\n" + RESET, p.id.value)
        printf("Date: %s\n\n", p.createdAt)
        println("    " + p.action.summary)
        println("")
        for(line <- action(p.action))
          println("    " + line)
        println("")
      }
    }
  }
}

