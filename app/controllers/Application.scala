package controllers

import java.io.File
import play.api._
import play.api.mvc._
import play.api.data._
import org.codefirst.katze.core._

object Application extends Controller {
  import play.api.data.Forms._

  def store =
    new Store(new File(".katze"))

  val ticketForm  = Form {
    mapping(
      "subject" -> nonEmptyText
    )(Ticket.make(_, Open))( t => Some(t.subject))
  }

  def index = Action {
    val tickets = store.current.tickets
    Ok(views.html.index(tickets))
  }

  def newTicketForm = Action {
    val form = ticketForm.fill(Ticket.make("", Open))
    Ok(views.html.newTicket(form))
  }

  def newTicket = Action { implicit request =>
    val ticket = ticketForm.bindFromRequest.get
    store.apply(Patch.make(AddAction(ticket)))
    Redirect(routes.Application.index)
  }

  def editTicketForm(id : String) = Action {
    store.findTicket(id) match {
      case Right(t) =>
        val form = ticketForm.fill(t)
        Ok(views.html.editTicket(t, form))
      case Left(reason) =>
        BadRequest(reason)
    }
  }

  def editTicket(id : String) = Action { implicit request =>
    store.findTicket(id) match {
      case Right(t) =>
        val next = ticketForm.bindFromRequest.get
        store.apply(Patch.make(UpdateAction.subject(t, next.subject)))
        Redirect(routes.Application.index)
      case Left(reason) =>
        BadRequest(reason)
    }
  }

}
