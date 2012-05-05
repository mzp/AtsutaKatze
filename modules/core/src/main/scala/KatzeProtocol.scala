package org.codefirst.katze.core

import sjson.json._
import dispatch.json._
import org.codefirst.katze.core.scm.Commit


object KatzeProtocol extends DefaultProtocol {
  import JsonSerialization._

  implicit def idFormat[T] : Format[ID[T]] =
    wrap("value")(_.value, ID.apply _)

  implicit val StatusFormat : Format[Status] =
    wrap("type")( _.toString,
                 ( _ : String).toLowerCase match {
                   case "open" => Open
                   case "close" => Close
                 })

  implicit val TicketFormat : Format[Ticket] =
    asProduct3("uuid", "subject", "status")(Ticket.apply _)(Ticket.unapply(_).get)

  implicit val ConfigFormat : Format[Config] =
    asProduct3("title", "scm", "defaultUrl")(Config.apply _)(Config.unapply(_).get)

  implicit val AddActionFormat : Format[AddAction] =
    wrap("ticket")(_.ticket, AddAction.apply _)

  implicit val UpdateActionFormat : Format[UpdateAction] =
    asProduct2("from", "to")(UpdateAction.apply _)(UpdateAction.unapply(_).get)

  implicit val DeleteActionFormat : Format[DeleteAction] =
    wrap("ticket")(_.ticket, DeleteAction.apply _)

  implicit val DateFormat : Format[java.util.Date] =
    wrap("date")(_.getTime(), new java.util.Date((_ : Long)))


  def multiple[T](x : Format[T])(y : Format[T]) : Format[T] =
    new Format[T] {
      def writes(t: T) =
        try {
          tojson(t)(x)
        } catch {
          case _ =>
            tojson(t)(y)
        }

      def reads(json: JsValue) =
        try {
          fromjson(json)(x)
        } catch {
          case _ =>
            fromjson(json)(y)
        }
    }

  implicit val CommitFormat : Format[Commit] =
    multiple {
      asProduct4("id", "author", "message","createdAt")(Commit.apply _)(Commit.unapply(_).get)
    } {
      asProduct3("id", "author", "message") {
        (id : String, author : String, message : String) =>
          Commit(id, author, message)
      } { c =>
        (c.id, c.author, c.message)
      }
    }

  implicit object ActionFormat extends Format[Action] {
    def reads(json : JsValue) =
      json match {
        case JsObject(map) =>
          map(JsString("type")) match {
            case JsString("add") =>
              fromjson[AddAction](map( JsString("action")))
            case JsString("update") =>
              fromjson[UpdateAction](map( JsString("action")))
            case JsString("del") =>
              fromjson[DeleteAction](map( JsString("action")))
            case _ =>
              throw new RuntimeException("unknown type patch")
          }
        case _ =>
          throw new RuntimeException("obj expected")
      }

    def writes(action : Action) :JsValue =
      action match {
        case act@AddAction(_) =>
          JsObject(Map(JsString("type")   -> JsString("add"),
                       JsString("action") -> tojson(act)))
        case act@UpdateAction(_,_) =>
          JsObject(Map(JsString("type")   -> JsString("update"),
                       JsString("action") -> tojson(act)))
        case act@DeleteAction(_) =>
          JsObject(Map(JsString("type")   -> JsString("del"),
                       JsString("action") -> tojson(act)))

      }
  }

  implicit val PatchFormat : Format[Patch] =
    asProduct4("id", "action", "depends" , "createdAt")(Patch.apply _)(Patch.unapply(_).get)
}
