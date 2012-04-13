package org.codefirst.katze.core.store

import dispatch._
import dispatch.json.{JsValue, JsObject, JsString}
import java.io.InputStream

trait HttpExecutor {
  def put(url : String, body : String) : Unit
  def get[T](url : String)(f : InputStream => T) : T
}

object DefaultHttpExecutor extends HttpExecutor {
  def put(url : String, body : String)  =
    Http(dispatch.url(url) <<< body >|)

  def get[T](url : String)(f : InputStream => T) =
    Http(dispatch.url(url) >> f)
}

class HttpStore(uri : java.net.URI,
                http : HttpExecutor = DefaultHttpExecutor) extends Store {
  import org.codefirst.katze.core._

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
}

