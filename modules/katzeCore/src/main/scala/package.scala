package org.codefirst.katze.core

import java.util.UUID
object ID {
  def get =
    UUID.randomUUID.toString
}
