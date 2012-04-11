package org.codefirst.katze.core.store

import sjson.json._
import dispatch.json._

trait Store {
  def read(name : String) : Option[JsValue]
  def write(name : String, value : JsValue) : Unit
}
