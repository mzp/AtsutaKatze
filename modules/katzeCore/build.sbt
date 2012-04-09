import AssemblyKeys._ // put this at the top of the file

seq(assemblySettings: _*)

libraryDependencies += "org.specs2" %% "specs2" % "1.8.2" % "test"

libraryDependencies += "net.debasishg" %% "sjson" % "0.17"

libraryDependencies += "com.beust" % "jcommander" % "1.23"

scalacOptions += "-deprecation"

scalacOptions += "-unchecked"


libraryDependencies += "redis.clients" % "jedis" % "2.0.0"
