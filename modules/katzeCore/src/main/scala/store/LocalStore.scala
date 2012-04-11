package org.codefirst.katze.core.store

import java.io.{File, InputStream, FileInputStream, FileOutputStream}
import dispatch.json.JsValue

class LocalStore(root : File) extends Store {
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

  def read(name : String) =
    sure {
      using( open_in( file(name) ) ) { is =>
        JsValue.fromStream(is)
      }
    }

  def write(name : String, value : JsValue) {
    using( open_out( file(name)) ) { os =>
      os.write(value.toString.getBytes)
    }
  }
}
