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
    mapping("subject" -> nonEmptyText)(Ticket.make(_, Open))( t => Some(t.subject))
  }

  def index = Action {
    val tickets = store.current.tickets
    Ok(views.html.index(tickets))
  }

  def newTicketForm = Action {
    Ok(views.html.newTicket())
  }

  def newTicket = Action { implicit request =>
    val ticket = ticketForm.bindFromRequest.get
    store.apply(Patch.make(AddAction(ticket)))
    Redirect(routes.Application.index)
  }
}
