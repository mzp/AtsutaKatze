package org.codefirst.katze.core.store

import java.io._
import sjson.json.{Reads,Writes, JsonSerialization}
import dispatch.json.JsValue

class SSHStore(uri : java.net.URI, exec : String => String = SSHStore.shell _) extends Store {
  import JsonSerialization._
  import org.codefirst.katze.core._

  def read[T](name : String)(implicit fjs : Reads[T]) : Option[T] = {
    val ret = exec(ssh("cat %s".format(path(name))))
    sure {
      val json = JsValue.fromString(ret)
      fromjson[T](json)
    }
  }

  def write[T](name : String, obj : T)(implicit fjs : Writes[T]) {
    val json = tojson(obj)
    exec(ssh("mkdir -p %s").format(dir(name)))
    exec("echo '%s' | %s".format(json,
                                ssh("cat > %s").format(path(name))))
  }

  private def ssh(cmd : String) : String = {
    val port =
      if(uri.getPort == -1)
        22
      else
        uri.getPort
    "ssh -p %d %s '%s'".format(port, uri.getHost, cmd)
  }

  private def path(name : String) : String = {
    val file = new File(uri.getPath, name)
    file.getAbsolutePath
  }

  private def dir(name : String) : String = {
    val file = new File(uri.getPath, name)
    file.getParentFile.getAbsolutePath
  }
}

object SSHStore {
  import org.codefirst.katze.core._
  def readAll(is : InputStream) =
    using( new ByteArrayOutputStream() ) { case bas =>
      val buffer = new Array[Byte](1024)
      var len = buffer.length

      while(len >= buffer.length) {
        len = is.read(buffer)
        if(len > 0) {
          bas.write(buffer, 0, len)
        }
      }

      new String(bas.toByteArray)
    }

  def shell(cmd : String) : String = {
    println(cmd)
    val process = Runtime.getRuntime().exec(Array("/bin/sh", "-c", cmd))
    process.waitFor
    val is = process.getInputStream()
    readAll(is)
  }
}
