package org.codefirst.katze.core.scm

import org.specs2.mutable._
import org.codefirst.katze.core._

class GitSpec extends Specification {
  "ローカルレポジトリ" should {
    val command = """
rm -rf /tmp/katze-test/git
mkdir -p /tmp/katze-test/git
cd /tmp/katze-test/git/
git init
touch A
git add A
git commit -m 'refs: other-id'
touch B
git add B
git commit -m 'refs: some-id'
"""
    Runtime.getRuntime.exec(Array("sh","-c", command))

    val git = new Git("/tmp/katze-test/git")
    val t = git.fetch(git.init)
    "ログを取得できる(失敗)" in {
      git.commits(t, ID("non-exist")) must have size(0)
    }

    "ログを取得できる" in {
      git.commits(t, ID("some-id")) must have size(1)
    }
  }
}

