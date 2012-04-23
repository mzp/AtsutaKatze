package org.codefirst.katze.core.store

import org.specs2.mutable._
import org.codefirst.katze.core._
import dispatch.json._

class RedisStoreSpec extends Specification {
  import KatzeProtocol._

  val store =
    new RedisStore("redis://localhost:6379")

  val rand =
    new scala.util.Random

  val value = {
    val n = rand.nextInt.toString
    JsValue.fromString(n)
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
}
