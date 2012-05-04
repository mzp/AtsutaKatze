package org.codefirst.katze.cli

import scala.collection.mutable.{ListBuffer, Map => MMap}
import com.beust.jcommander.JCommander
import org.codefirst.katze.core.Repository

trait Command {
  type t
  def params : t
  def description : String
  def action(repos : Option[Repository], params : t) : Unit
}

trait CommandDefinition {
  type Action = JCommander => Unit
  val actions : ListBuffer[Action] = ListBuffer()
  val commands = MMap[String, Command]()

  def define[T](name : String, command : Command) {
    actions += { (jc : JCommander) =>
      jc.addCommand(name, command.params)
    }
    commands += name -> command
  }

  def apply(jc : JCommander) {
    for(action <- actions)
      action(jc)
  }

  def command(name : String) : Command =
    commands(name)

  def withRepos[T](name : String, desc : String)(p : T)(act : (Repository, T) => Unit) =
    define(name, new Command {
      type t = T
      def description =
        desc

      def params =
        p

      def action(repos : Option[Repository], params : T) {
        repos match {
          case Some(repos) =>
            act(repos, params)
          case None =>
            throw new RuntimeException("not found repos")
        }
      }})


  def withoutRepos[T](name : String, desc : String)(p : T)(act : T => Unit) =
    define(name, new Command {
      type t = T
      def description =
        desc

      def params =
        p

      def action(repos : Option[Repository], params : T) {
        repos match {
          case Some(repos) =>
            throw new RuntimeException("already init")
          case None =>
            act(params)
        }
      }})
}


class KatzeCommander(obj : AnyRef, definition : CommandDefinition)
    extends JCommander(obj,  (null : java.util.ResourceBundle ))
{
  import scala.collection.JavaConverters._
  definition(this)

  override def getCommandDescription(name : String) : String = {
    definition.command(name).description
  }
}
