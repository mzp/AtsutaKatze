package org.codefirst.katze.core.scm

import org.specs2.mutable._
import org.codefirst.katze.core._

class GitHubSpec extends Specification {
  val git = new GitHub("https://github.com/mzp/test")
  val state = git.fetch(git.init)

  "ログを取得できる(失敗)" in {
    git.commits(state, ID("non-exist")) must have size(0)
  }

  "ログを取得できる" in {
    git.commits(state, ID("some-key")) must have size(1)
  }
}

