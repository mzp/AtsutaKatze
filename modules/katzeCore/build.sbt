import AssemblyKeys._ // put this at the top of the file

seq(assemblySettings: _*)

libraryDependencies += "org.specs2" %% "specs2" % "1.8.2" % "test"

libraryDependencies += "net.debasishg" %% "sjson" % "0.17"

libraryDependencies += "com.beust" % "jcommander" % "1.23"

libraryDependencies += "redis.clients" % "jedis" % "2.0.0"

libraryDependencies += "net.databinder" %% "dispatch-http" % "0.8.8"

libraryDependencies += "junit" % "junit" % "4.9" % "test"

scalacOptions += "-deprecation"

scalacOptions += "-unchecked"

testOptions in Test += Tests.Argument("junitxml", "console")


libraryDependencies += "org.eclipse.jgit" % "org.eclipse.jgit" % "1.3.0.201202151440-r"
