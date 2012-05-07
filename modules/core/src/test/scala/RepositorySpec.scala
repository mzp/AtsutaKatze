package org.codefirst.katze.core

import org.specs2.mutable._
import java.io._
import sjson.json._
import dispatch.json._
import org.codefirst.katze.core._
import scala.collection.{mutable}

class RepositorySpec extends Specification {
  class MemoryStore extends store.Store {
    val map : mutable.Map[String, JsValue] = mutable.Map()

    def read(name : String) =
      map.get(name)

    def write(name : String, obj : JsValue) {
      map.update(name, obj)
    }

    def remove(name : String) {
      map.remove(name)
    }
  }

  def repository() =
    new Repository(new MemoryStore)

  def noDepends(p : Patch) =
      p.copy(depends = None)

  "初期状態" should {
    "headが空" in {
      repository().head must_== None
    }
  }

  "パッチが追加された状態" should {
    val repos = repository()
    val p1 = Patch.make(AddAction(Ticket.make("foo",Open)))
    val p2 = Patch.make(AddAction(Ticket.make("bar",Open)))
    repos.apply(p1)
    repos.apply(p2)

    "headが更新される" in {
      repos.head.map(noDepends(_)) must_== Some(p2)
    }

    "パッチが取得できる" in {
      repos.patch(p1.id).map(noDepends(_)) must_== Some(p1)
      repos.patch(p2.id).map(noDepends(_)) must_== Some(p2)
    }

    "パッチ一覧が取得できる" in {
      repos.changes.map(noDepends(_)) must_== List(p2, p1)
    }
  }

  "チケット一覧" should {
    val repos = repository()
    val t1 = Ticket.make("foo",Open)
    val t2 = Ticket.make("foo",Open)
    val t3 = Ticket.make("foo",Open)

    repos.apply(Patch.make(AddAction(t1)))
    repos.apply(Patch.make(AddAction(t2)))
    repos.apply(Patch.make(AddAction(t3)))
    repos.apply(Patch.make(DeleteAction(t1)))

    "現在のチケット一覧が取得できる" in {
      repos.tickets must_== List(t3, t2)
    }

    "チケットが取得できる" in {
      repos.ticket(t2.id.value) must_== Right(t2)
    }

    "IDの一部でもチケットが取得できる" in {
      repos.ticket(t2.id.value.substring(0, 5)) must_== Right(t2)
    }
  }

  "プロジェクトごとの設定" should {
    "diffを生成しない" in {
      val repos =  repository()
      repos.updateConfig(_)
      repos.changes must_== List()
    }

    "初期状態は空" in {
      val repos =  repository()
      repos.config must_== Config.empty
    }

    "更新できる" in {
      val repos =  repository()
      repos.updateConfig(_.copy(scm = Some("x")))
      repos.config.scm must_== Some("x")
    }
  }

  "push/pull" should {
    val src = repository()
    val p1 = Patch.make(AddAction(Ticket.make("foo",Open)))
    val p2 = Patch.make(AddAction(Ticket.make("bar",Open)))

    src.apply(p1)
    src.apply(p2)

    "push可能" in {
      val repos = repository()
      Repository.copy(src, repos)
      repos.changes.map(noDepends(_)) must_== List(p2, p1)
    }

    "push可能" in {
      val repos = repository()
      repos.apply(p1)
      Repository.copy(src, repos)
      repos.changes.map(noDepends(_)) must_== List(p2, p1)
    }

    "push不可" in {
      val repos = repository()
      repos.apply(p2)
      Repository.copy(src, repos) must throwA[RuntimeException]
    }

    "push --force" in {
      val repos = repository()
      repos.apply(p2)
      Repository.forceCopy(src, repos)
      repos.changes.map(noDepends(_)) must_== List(p2, p1)
    }
  }
}

