package org.codefirst.katze.cli

import scala.collection.mutable.ListBuffer
import com.beust.jcommander.JCommander
import org.codefirst.katze.core.Store

trait Command {
  def execute(store : Store) : Unit
  def description : String
}

trait CommandDefinition {
  type Action = JCommander => Unit
  val actions : ListBuffer[Action] = ListBuffer()

  def define(name : String)(cmd : Command) = {
    val action = (jc : JCommander) =>
      jc.addCommand(name, cmd)
    actions += action
  }

  def apply(jc : JCommander) {
    for(action <- actions)
      action(jc)
  }
}

class KatzeCommander(obj : AnyRef) extends JCommander(obj,  (null : java.util.ResourceBundle )) {
  import scala.collection.JavaConverters._

  override def getCommandDescription(name : String) : String = {
    val objs = getCommands.asScala(name).getObjects.asScala
    objs.headOption match {
      case Some(x) if x.isInstanceOf[Command] =>
        x.asInstanceOf[Command].description
      case _ =>
        super.getCommandDescription(name)
    }
  }
}



