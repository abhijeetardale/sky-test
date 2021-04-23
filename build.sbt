import scoverage.ScoverageKeys

lazy val phoneCompany = (project in file(".")).settings(
  Seq(
    name := "sky-test",
    version := "1.0",
    scalaVersion := "2.12.3",
    ScoverageKeys.coverageExcludedFiles := ".*com.sky.bookmarking*;"
  )
)

mainClass in (Compile, run) := Some("com.phone.Main")

libraryDependencies ++= Seq(
  "com.google.inject" % "guice" % "4.2.2",
  "org.mockito" % "mockito-core" % "3.3.3" % "test",
  "org.scalacheck" %% "scalacheck" % "1.14.1" % "test",
  "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0" % "test"
)
