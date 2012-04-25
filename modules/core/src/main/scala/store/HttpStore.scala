package org.codefirst.katze.core.store

import dispatch.json.{JsValue, JsObject, JsString}
import java.io.InputStream
import org.codefirst.katze.core._

class HttpStore(uri : java.net.URI,
                http : HttpExecutor = DefaultHttpExecutor) extends Store {

  val entry =
    uri.toString + "/api/v1/"

  def read(name : String) = {
    sure {
      http.get[JsValue](entry + name) { in =>
        val json = JsValue.fromStream(in)
        json.asInstanceOf[JsObject].self(JsString("content"))
      }
    }
  }

  def write(name : String, value : JsValue) {
    http.put(entry + name, value.toString)
  }

  def remove(name : String) {
    http.delete(entry + name)
  }
}

