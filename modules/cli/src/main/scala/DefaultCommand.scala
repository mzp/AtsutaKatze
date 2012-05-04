package org.codefirst.katze.cli
import java.io.File
import com.beust.jcommander._
import org.codefirst.katze.core._

object DefaultCommands extends CommandDefinition {
  import scala.collection.JavaConverters._
  object NoParams

  withoutRepos("init", "initialize katze repository")(NoParams) { _ =>
    val katze = new File(".katze")
    katze.mkdirs
  }

  withRepos("list", "show all tickets") {
    new Object {
      @Parameter(names = Array("-a","--active"), description = "only active ticket")
      val active_only : Boolean = false
    } } { (repos, params) =>
      val status : Map[Status, String] =
        Map(Open  -> " ",
            Close -> "x" )

      for{
        t <- repos.tickets
        if (params.active_only == false) || (t.status == Open)
      } {
        printf("%s %s %s\n",
               t.id.value,
               status(t.status),
               t.subject)
      }
    }

  withRepos("add", "add new ticket") {
    new Object {
      @Parameter(names = Array("-s"), description = "subject")
      val subject : String = ""
    }
  } { (repos, params) =>
    val ticket = Ticket.make(params.subject,Open)
    repos.apply(Patch.make(AddAction(ticket)))
  }

  withRepos("status", "change status") {
    new Object {
      @Parameter(description = "")
      val args : java.util.List[String] = null
    }
  } { (repos, params) =>
    params.args.asScala.toList match {
      case List(status, id) =>
        repos.ticket(id) match {
          case Left(str) =>
            println(str)
          case Right(t) =>
            val patch =
              Patch.make(UpdateAction.status(t, Status.fromString(status)))
            repos.apply(patch)
        }
      case _ =>
        throw new RuntimeException("too much arguments")
    }
  }

  withRepos("mv", "rename ticket") {
    new Object {
      @Parameter(names = Array("-s"), description = "subject")
      val subject : String = ""

      @Parameter(description = "")
      val ids : java.util.List[String] = null
    }
  } { (repos, params) =>
    for(id <- params.ids.asScala) {
      repos.ticket(id) match {
        case Left(str) =>
          println(str)
        case Right(t) =>
          repos.apply(Patch.make(UpdateAction.subject(t, params.subject)))
      }
    }
  }

  withRepos("rm", "remove ticket") {
    new Object {
      @Parameter(description = "")
      val ids : java.util.List[String] = null
    }
  } { (repos, params) =>
    for(id <- params.ids.asScala) {
      repos.ticket(id) match {
        case Left(str) =>
          println(str)
        case Right(t) =>
          repos.apply(Patch.make(DeleteAction(t)))
      }
    }
  }

  withRepos("changes", "show changes")(NoParams) { (repos, _)  =>
    for(p <- repos.changes) {
      printf("%s %s\n", p.id.short, p.action.summary)
    }
  }

  class SavedUrlParams {
    @Parameter(names = Array("-s","--save"), description = "save push url")
    val save : Boolean = false

    @Parameter(names = Array("-f","--force"), description = "force")
    val force : Boolean = false

    @Parameter(description = "")
    val targets : java.util.List[String] = new java.util.ArrayList()
  }

  def Url(action : (String, Repository, SavedUrlParams) => Unit) (repos : Repository, params : SavedUrlParams) {
    val url = params.targets.asScala.headOption orElse {
      repos.config.defaultUrl
    }
    url match {
      case Some(u) =>
        action(u, repos, params)
      if(params.save) {
        repos.updateConfig {
          _.copy(defaultUrl = Some(u))
        }
      }
      case None =>
        throw new RuntimeException("please specify push url")
    }
  }

  def copy(params : SavedUrlParams, src : Repository, dest : Repository) =
    if(params.force)
      Repository.forceCopy(src, dest)
    else
        Repository.copy(src, dest)

  withRepos("push", "push changes to remote repository")(new SavedUrlParams) { Url{(url, repos, params) =>
    println("push to " + url)
    val dest = Repository.open(url)
    copy(params, repos, dest)
  } }

  withRepos("pull","pull changes from remote repository")(new SavedUrlParams) { Url{(url, repos, params) =>
    println("pull from " + url)
    val dest = Repository.open(url)
    copy(params, dest, repos)
  } }

  withRepos("fetch", "fetch scm changes")(NoParams) { (repos, _) =>
    repos.fetch
  }

  withRepos("scm","set/get repository")( new Object {
    @Parameter(description = "")
    var repositories : java.util.List[String] = null
  }) { (repos, params) =>
    Option(params.repositories).flatMap(_.asScala.headOption) match {
      case None =>
        val scm =
          repos.config.scm getOrElse { "none" }
        println(scm)
      case Some(url) =>
        // write mode
        repos.updateConfig {
          _.copy(scm = Some(url))
        }
    } }

  withRepos("commits", "show commits")(new Object {
    @Parameter(names = Array("-n","--no-fetch"), description = "no fetch")
    val no_fetch : Boolean = false

    @Parameter(description = "")
    var tickets : java.util.List[String] = null
  }) { (repos, params) =>
    def either[A,B](x : Either[A,B]) =
      x match {
        case Right(x) => Some(x)
        case Left(_)  => None
      }

    if( !params.no_fetch )
      repos.fetch

    val commits = for {
      id     <- Option(params.tickets).flatMap(_.asScala.headOption)
      ticket <- either(repos.ticket(id))
    } yield repos.commits(ticket)
    commits getOrElse { List() } foreach { case c =>
      printf("[%s] %s\n%s\n", c.id, c.author, c.message)
  } }
}
