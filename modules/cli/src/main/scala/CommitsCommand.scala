package org.codefirst.katze.cli
import org.codefirst.katze.core.scm.Commit
import com.beust.jcommander._
import scala.collection.JavaConverters._
import Console._
import java.util.Date

object CommitsCommand {
  def either[A,B](x : Either[A,B]) =
    x match {
      case Right(x) =>
        Some(x)
      case Left(_)  =>
        None
    }

  def show(c : Commit) = {
    printf(YELLOW + "commits %s\n" + RESET, c.id)
    printf("Author: %s\n", c.author)
    printf("Date: %s\n\n", c.createdAt)
    for(line <- c.message.lines)
      println("    " + line)
  }

  def apply(definition : CommandDefinition) {
    definition.withRepos("commits", "show commits")(new Object {
      @Parameter(names = Array("-n","--no-fetch"), description = "no fetch")
      val no_fetch : Boolean = false

      @Parameter(description = "")
      var tickets : java.util.List[String] = null
    }) { (repos, params) =>
      if( !params.no_fetch )
        repos.fetch

      val commits = for {
        id     <- Option(params.tickets).flatMap(_.asScala.headOption)
        ticket <- either(repos.ticket(id))
      } yield repos.commits(ticket).toList
      Commit.uniq(commits getOrElse { List() }) foreach { c =>
        c match {
          case Left(n) =>
            printf(YELLOW+"%d now commits: %s...%s \n"+RESET,
                   n.count,
                   n.from.toString,
                   n.to.toString)
          case Right(c) =>
            show(c)
        }
        println("")
      } }
  }
}
