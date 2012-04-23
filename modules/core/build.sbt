libraryDependencies += "org.specs2" %% "specs2" % "1.8.2" % "test"

libraryDependencies += "net.debasishg" %% "sjson" % "0.17"

libraryDependencies += "redis.clients" % "jedis" % "2.0.0"

libraryDependencies += "net.databinder" %% "dispatch-http" % "0.8.8"

libraryDependencies += "junit" % "junit" % "4.9" % "test"

libraryDependencies += "org.eclipse.jgit" % "org.eclipse.jgit" % "1.3.0.201202151440-r"

scalacOptions += "-deprecation"

scalacOptions += "-unchecked"

testOptions in Test += Tests.Argument("junitxml", "console")

