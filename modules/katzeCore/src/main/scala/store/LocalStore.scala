package org.codefirst.katze.core.store

import java.io.{File, InputStream, FileInputStream, FileOutputStream}
import sjson.json.{Reads,Writes, JsonSerialization}
import dispatch.json.JsValue

class LocalStore(root : File) extends Store {
  import JsonSerialization._
  import org.codefirst.katze.core._

  root.mkdirs

  private def open_in(file : File) =
    new FileInputStream(file)

  private def open_out(file : File) =
    new FileOutputStream(file)

  private def file(name : String) =
    tee(new File( root, name )) { file =>
      file.getParentFile.mkdirs
    }

  def read[T](name : String)(implicit fjs : Reads[T]) : Option[T] =
    sure {
      using( open_in( file(name) ) ) { is =>
        val json = JsValue.fromStream(is)
        fromjson[T](json)
      }
    }

  def write[T](name : String, obj : T)(implicit fjs : Writes[T]) {
    using( open_out( file(name)) ) { os =>
      os.write(tojson(obj).toString.getBytes)
    }
  }
}
