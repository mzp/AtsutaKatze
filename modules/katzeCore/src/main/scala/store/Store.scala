package org.codefirst.katze.core.store

import sjson.json._
import dispatch.json._

trait Store {
  def read[T](name : String)(implicit fjs : Reads[T]) : Option[T]
  def write[T](name : String, obj : T)(implicit fjs : Writes[T]) : Unit
}
