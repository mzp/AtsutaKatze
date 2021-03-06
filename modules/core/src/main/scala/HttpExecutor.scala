package org.codefirst.katze.core

import java.io.InputStream
import dispatch._

trait HttpExecutor {
  def put(url : String, body : String) : Unit
  def get[T](url : String)(f : InputStream => T) : T
  def delete(url : String) : Unit
}

object DefaultHttpExecutor extends HttpExecutor {
  def put(url : String, body : String)  =
    Http(dispatch.url(url) <<< body <:< Map("Content-Type" -> "application/json") >|)

  def get[T](url : String)(f : InputStream => T) =
    Http(dispatch.url(url) >> f)

  def delete(url : String) =
    Http(dispatch.url(url).DELETE >|)
}
