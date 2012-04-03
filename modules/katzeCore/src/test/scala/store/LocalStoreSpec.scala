package org.codefirst.katze.core.store

import org.specs2.mutable._

import java.io._
import sjson.json._
import dispatch.json._

import org.codefirst.katze.core._

class LocalStoreSpec extends Specification {
  import KatzeProtocol._

  val store = new LocalStore(new File(".test"))
  type T = ID[Any]
  val  x : T = ID("xxx")

  "read" should {
    "キーが存在しなければ失敗する" in {
      store.read[T]("non-exist-key") must_== None
    }
  }

  "write" should {
    "書き込んだ値を読める" in {
      store.write("key", x)
      store.read[T]("key") must_== Some(x)
    }
  }
}
