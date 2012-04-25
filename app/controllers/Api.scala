package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.libs.json._

object Api extends Controller {
  import Json._

  def get(name : String) = Action {
    val ret = for {
      s <- Katze.store
      value <- s read name
    } yield Json.parse(value.toString)


    ret match {
      case Some(json) =>
        Ok(toJson(Map("status"-> JsString("ok"),
                      "content" ->json)))
      case None =>
        NotFound(toJson(Map("status" -> "ng", "name" -> name)))
    }
  }

  def put(name : String) = Action(parse.json) { request =>
    val s = Json.stringify(request.body)
    val json = dispatch.json.JsValue.fromString(s)
    Katze.store.map(_.write(name, json))
    Ok(toJson(Map("status" -> "ok")))
  }

  def delete(name : String) = Action {
    Katze.store.map(_.remove(name))
    Ok(toJson(Map("status" -> "ok")))
  }
}
