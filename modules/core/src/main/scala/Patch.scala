package org.codefirst.katze.core

import java.util.Date

case class Patch(
  id        : ID[Patch],
  action    : Action,
  depends   : Option[ID[Patch]],
  createdAt : Date) {
}

object Patch {
  def make(action : Action) =
    Patch(ID.get, action, None, new Date)
}
