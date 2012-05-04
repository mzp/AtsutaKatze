package org.codefirst.katze.core.scm

import org.specs2.mutable._
import org.codefirst.katze.core._
import java.util.GregorianCalendar

class GitSpec extends Specification {
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
git commit --author='mzp' -m 'refs: some-id'
"""
  Runtime.getRuntime.exec(Array("sh","-c", command))
  val git = new Git("/tmp/katze-test/git")
  val state = git.fetch(git.init)

  "存在しないチケット番号" should {
    "ログを取得できない" in {
      git.commits(state, ID("non-exist")) must have size(0)
    }
  }

  "正しいチケット番号" should {
    "ログを取得できる" in {
      git.commits(state, ID("some-id")) must have size(1)
    }

    def commit =
      git.commits(state, ID("some-id")).head


    "ログの内容が正しい" in {
      println(commit.id)
      commit.message must_== "refs: some-id"
    }

    "authorが正しい" in {
      commit.author must_== "mzp"
    }

  }
}

