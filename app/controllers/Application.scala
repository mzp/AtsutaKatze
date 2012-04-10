package controllers

import java.io.File
import play.api._
import play.api.mvc._
import play.api.data._
import org.codefirst.katze.core._
import org.codefirst.katze.core.store._


object Application extends Controller {
  import play.api.data.Forms._
  import play.api.Play.current


  val storeMap = Map[String, Configuration => Option[Store]](
    "local" -> { cf =>
      cf.getString("path") map { path =>
        new LocalStore(new File(path))
      }
     },
    "redis" -> { cf =>
      cf.getString("url") map { url =>
        new RedisStore(url)
      }
    }
  )

  val store : Option[Store] = for {
    config     <- Play.configuration.getConfig("store")
    storeType  <- config.getString("type")
    params     <- config.getConfig(storeType)
    store      <- storeMap(storeType)(params)
  } yield store

  def repository : Repository =
    store match {
      case Some(s) =>
        new Repository(s)
      case None =>
        throw new RuntimeException("store is not configured")
    }

  val ticketForm  = Form {
    mapping(
      "subject" -> nonEmptyText
    )(Ticket.make(_, Open))( t => Some(t.subject))
  }

  def index = Action {
    val tickets = repository.current.tickets
    Ok(views.html.index(tickets))
  }

  def newTicketForm = Action {
    val form = ticketForm.fill(Ticket.make("", Open))
    Ok(views.html.newTicket(form))
  }

  def newTicket = Action { implicit request =>
    val ticket = ticketForm.bindFromRequest.get
    repository.apply(Patch.make(AddAction(ticket)))
    Redirect(routes.Application.index)
  }

  def editTicketForm(id : String) = Action {
    repository.findTicket(id) match {
      case Right(t) =>
        val form = ticketForm.fill(t)
        Ok(views.html.editTicket(t, form))
      case Left(reason) =>
        BadRequest(reason)
    }
  }

  def editTicket(id : String) = Action { implicit request =>
    repository.findTicket(id) match {
      case Right(t) =>
        val next = ticketForm.bindFromRequest.get
        repository.apply(Patch.make(UpdateAction.subject(t, next.subject)))
        Redirect(routes.Application.index)
      case Left(reason) =>
        BadRequest(reason)
    }
  }

  def removeTicket(id : String) = Action {
    repository.findTicket(id) match {
      case Right(t) =>
        repository.apply(Patch.make(DeleteAction(t)))
        Redirect(routes.Application.index)
      case Left(reason) =>
        BadRequest(reason)
    }
  }

  def changes = Action {
    val changes = repository.changes
    Ok(views.html.changes(changes))
  }
}
