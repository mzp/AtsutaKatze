package org.codefirst.katze.core.store

import org.specs2.mutable._

import java.io._
import sjson.json._
import dispatch.json._

import org.codefirst.katze.core._

class SSHStoreSpec extends Specification {
  args(sequential=true)
  import KatzeProtocol._

  type T = ID[Any]
  val  x : T = ID("42")

  "read" should {
    var lastCommand = ""
    val store = new SSHStore(new java.net.URI("ssh://example.com:22/tmp"), { cmd =>
      lastCommand = cmd
      "{ \"value\" : \"42\" }"
    })
    val ret = store.read[T]("path/to/file")

    "ssh catでファイルを確認する" in {
      lastCommand must_== "ssh -p 22 example.com 'cat /tmp/path/to/file'"
    }

    "返り値をちゃんとパースする" in {
      ret must_== Some(x)
    }
  }

  "write" should {
    var lastCommand = ""
    val store = new SSHStore(new java.net.URI("ssh://example.com/tmp"), { cmd =>
      lastCommand = cmd
      ""
    })
    store.write("path/to/file", x)

    "ssh catでファイルを書き込む" in {
      lastCommand must_== "cat '{\"value\" : \"42\"}' | ssh -p 22 example.com 'cat > /tmp/path/to/file'"
    }
  }

  "shell" should {
    "get result" in {
      SSHStore.shell("echo hi") must_== "hi\n"
    }

   "pipe" in {
      SSHStore.shell("seq 10 | head -1") must_== "1\n"
   }
  }
}
