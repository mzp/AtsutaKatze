package org.codefirst.katze

package object core {
  def using[A <: { def close() : Unit }, B](x : A)( f : A => B) : B =
    try {
      f(x)
    } finally {
      x.close()
    }

  def sure[A](body : => A) : Option[A] =
    try {
      Some(body)
    } catch { case _ =>
      None
    }

  def tee[A]( x : A)(action : A => Unit) : A = {
    action(x)
    x
  }
}
