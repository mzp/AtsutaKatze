libraryDependencies += "junit" % "junit" % "4.9" % "test"

testOptions in Test += Tests.Argument("junitxml")
