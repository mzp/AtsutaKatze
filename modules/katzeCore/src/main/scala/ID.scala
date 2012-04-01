package org.codefirst.katze.core

import java.util.UUID

case class ID[T](value : String) {
  def short =
    value.substring(0,5) + ".."
}

object ID {
  def get[T] : ID[T] =
    ID(UUID.randomUUID.toString)
}
