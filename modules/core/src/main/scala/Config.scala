package org.codefirst.katze.core

case class Config(
  title : String,
  scm: Option[String],
  defaultUrl : Option[String]
)

object Config {
  def empty =
    Config("",None,None)
}
