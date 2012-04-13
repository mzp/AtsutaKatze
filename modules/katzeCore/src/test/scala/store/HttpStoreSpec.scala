package org.codefirst.katze.core.store

import java.net.URI
import java.io._

import org.specs2.mutable._
import org.codefirst.katze.core._
import dispatch.json._
import scala.collection.{mutable}

class HttpStoreSpec extends Specification {
  object MockHttpExecutor extends HttpExecutor {
    val map : mutable.Map[String, String] =
      mutable.Map()

    def json(str : String) =
      new ByteArrayInputStream(str.getBytes)

    def get[T](url : String)(f : InputStream => T) : T = {
      url match {
        case "http://localhost/api/v1/foo" =>
          f(json("{ \"status\" : \"ok\", \"content\" : 42 }"))
        case "http://localhost/api/v1/bar" =>
          f(json("{ \"status\" : \"error\" }"))
        case url =>
          f(json(map(url)))
      }
    }

    def put(url : String, body : String) {
      map(url) = "{ \"content\" : %s }".format(body)
    }
  }

  val store =
    new HttpStore(new URI("http://localhost"), MockHttpExecutor)

  "200 OK" in {
    store.read("foo") must_== Some(JsNumber(42))
  }

  "404 NotFound" in {
    store.read("bar") must_== None
  }

  "PUT" in {
    store.write("baz", JsNumber(1))
    store.read("baz") must_== Some(JsNumber(1))
  }
}
