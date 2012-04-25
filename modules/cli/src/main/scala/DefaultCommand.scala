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
  }}


  trait SavedUrl {
    @Parameter(names = Array("-s","--save"), description = "save push url")
    var save : Boolean = false


    @Parameter(names = Array("-f","--force"), description = "force")
    var force : Boolean =
      false

    @Parameter(description = "")
    var targets : java.util.List[String] =
      new java.util.ArrayList()


    def Url(repos : Repository)(action : String => Unit) {
      val url = targets.asScala.headOption orElse {
        repos.config.defaultUrl
      }
      url match {
        case Some(u) =>
          action(u)
          if(save) {
            repos.updateConfig {
              _.copy(defaultUrl = Some(u))
            }
          }
        case None =>
          throw new RuntimeException("please specify push url")
      }
    }

    def copy(src : Repository, dest : Repository) =
      if(force)
        Repository.forceCopy(src, dest)
      else
        Repository.copy(src, dest)
  }


  define("push") { new Command with SavedUrl {
    val description =
      "push local changes"

    def execute( repos : Repository) = Url(repos) { url =>
      println("push to " + url)
      val dest = Repository.open(url)
      copy(repos, dest)
    }
  } }

  define("pull") { new Command with SavedUrl {
    val description =
      "get remote changes"

    def execute( repos : Repository) = Url(repos) { url =>
      println("pull from " + url)
      val dest = Repository.open(url)
      copy(dest, repos)
    }
  } }

  define("fetch") { new Command {
    val description =
      "fetch scm changes"

    def execute( repos : Repository) {
      repos.fetch
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
