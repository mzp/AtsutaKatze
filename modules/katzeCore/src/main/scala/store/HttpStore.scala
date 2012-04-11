package org.codefirst.katze.core.store

import dispatch._
import dispatch.json._

class HttpStore(uri : java.net.URI) extends Store {
  import org.codefirst.katze.core._
  import Http._

  val entry =
    url(uri.toString) / "api" / "v1"

  def read(name : String) = {
    val x = sure {
      Http(entry / name >> { in =>
        val json = JsValue.fromStream(in)
        json.asInstanceOf[JsObject].self(JsString("content"))
      })
    }
    println(x)
    x
  }

  def write(name : String, value : JsValue) {
    val s : String = value.toString
    println(s)
    Http(entry / name <<< s >|)
  }
}
