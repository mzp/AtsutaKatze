package org.codefirst.katze.core.store

import org.specs2.mutable._

import java.io._
import sjson.json._
import dispatch.json._

import org.codefirst.katze.core._

class LocalStoreSpec extends Specification {
  import KatzeProtocol._

  val store = new LocalStore(new File(".test"))

  val rand =
    new scala.util.Random

  val value = {
    val x = rand.nextInt.toString
    JsValue.fromString(x)
  }

  "read" should {
    "キーが存在しなければ失敗する" in {
      store.read("non-exist-key") must_== None
    }
  }

  "write" should {
    "書き込んだ値を読める" in {
      store.write("key", value)
      store.read("key") must_== Some(value)
    }
  }

  "delete" should {
    "キーを削除できる" in {
      store.write("tmp_key", value)
      store.remove("tmp_key")
      store.read("tmp_key") must_== None
    }
  }
}
