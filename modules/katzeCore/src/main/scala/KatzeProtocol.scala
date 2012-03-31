package org.codefirst.katze.core

import sjson.json._
import dispatch.json._

object KatzeProtocol extends DefaultProtocol {
  import JsonSerialization._

  implicit val IDFormat : Format[ID] =
    wrap("value")(_.value, ID.apply _)

  implicit val StatusFormat : Format[Status] =
    wrap("type")( _.toString,
                 ( _ : String).toLowerCase match {
                   case "open" => Open
                   case "close" => Close
                 })

  implicit val TicketFormat : Format[Ticket] =
    asProduct3("uuid", "subject", "status")(Ticket.apply _)(Ticket.unapply(_).get)

  implicit val ProjectFormat : Format[Project] =
    wrap("tickets")(_.tickets, Project.apply _)

  implicit val AddActionFormat : Format[AddAction] =
    wrap("ticket")(_.ticket, AddAction.apply _)

  implicit object ActionFormat extends Format[Action] {
    def reads(json : JsValue) =
      json match {
        case JsObject(map) =>
          map(JsString("type")) match {
            case JsString("add") =>
              fromjson[AddAction](map( JsString("action")))
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
      }
  }

  implicit val DateFormat : Format[java.util.Date] =
    wrap("date")(_.getTime(), new java.util.Date((_ : Long)))

  implicit val PatchFormat : Format[Patch] =
    asProduct3("id", "action", "createdAt")(Patch.apply _)(Patch.unapply(_).get)
}
