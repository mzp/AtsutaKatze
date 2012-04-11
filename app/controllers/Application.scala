package controllers

import java.io.File
import play.api._
import play.api.mvc._
import play.api.data._
import org.codefirst.katze.core._
import org.codefirst.katze.core.store._

object Application extends Controller {
  import play.api.data.Forms._

  val ticketForm  = Form {
    mapping(
      "subject" -> nonEmptyText
    )(Ticket.make(_, Open))( t => Some(t.subject))
  }

  def index = Action {
    val tickets = Katze.repository.current.tickets
    Ok(views.html.index(tickets))
  }

  def newTicketForm = Action {
    val form = ticketForm.fill(Ticket.make("", Open))
    Ok(views.html.newTicket(form))
  }

  def newTicket = Action { implicit request =>
    val ticket = ticketForm.bindFromRequest.get
    Katze.repository.apply(Patch.make(AddAction(ticket)))
    Redirect(routes.Application.index)
  }

  def editTicketForm(id : String) = Action {
    Katze.repository.findTicket(id) match {
      case Right(t) =>
        val form = ticketForm.fill(t)
        Ok(views.html.editTicket(t, form))
      case Left(reason) =>
        BadRequest(reason)
    }
  }

  def editTicket(id : String) = Action { implicit request =>
    Katze.repository.findTicket(id) match {
      case Right(t) =>
        val next = ticketForm.bindFromRequest.get
        Katze.repository.apply(Patch.make(UpdateAction.subject(t, next.subject)))
        Redirect(routes.Application.index)
      case Left(reason) =>
        BadRequest(reason)
    }
  }

  def removeTicket(id : String) = Action {
    Katze.repository.findTicket(id) match {
      case Right(t) =>
        Katze.repository.apply(Patch.make(DeleteAction(t)))
        Redirect(routes.Application.index)
      case Left(reason) =>
        BadRequest(reason)
    }
  }

  def changes = Action {
    val changes = Katze.repository.changes
    Ok(views.html.changes(changes))
  }
}
