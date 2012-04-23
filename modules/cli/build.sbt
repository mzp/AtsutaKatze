import AssemblyKeys._ // put this at the top of the file

seq(assemblySettings: _*)

libraryDependencies += "com.beust" % "jcommander" % "1.23"

scalacOptions += "-deprecation"

scalacOptions += "-unchecked"

testOptions in Test += Tests.Argument("junitxml", "console")

