package controllers

import java.io.File

import play.api._
import play.api.Play.current

import org.codefirst.katze.core._
import org.codefirst.katze.core.store._

object Katze extends GlobalSettings {
  private val storeMap = Map[String, Configuration => Option[Store]](
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
}
