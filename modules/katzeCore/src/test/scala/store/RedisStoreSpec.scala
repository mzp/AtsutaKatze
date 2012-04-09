package org.codefirst.katze.core.store

import org.specs2.mutable._
import org.codefirst.katze.core._

class RedisStoreSpec extends Specification {
  import KatzeProtocol._

  val store =
    new RedisStore("redis://localhost:6379")

  val rand =
    new scala.util.Random

  type T = ID[Any]
  val  x : T = ID(rand.nextInt.toString)

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
