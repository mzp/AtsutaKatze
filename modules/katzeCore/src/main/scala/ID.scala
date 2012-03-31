package org.codefirst.katze.core

import java.util.UUID

case class ID(value : String) {
  def short =
    value.substring(0,5)
}

object ID {
  def get =
    ID(UUID.randomUUID.toString)
}
