val Http4sVersion = "0.19.0"
val LogbackVersion = "1.2.3"
val scalaPhashVersion = "1.1.0"
val fs2Version = "1.0.0"

lazy val root = (project in file("."))
  .enablePlugins(SbtTwirl)
  .settings(
    organization := "com.github.poslegm",
    name := "scalaphash-demo",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.12.6",
    resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
    scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-feature",
      "-language:higherKinds",
      "-Xfatal-warnings",
      "-Ypartial-unification"
    ),
    libraryDependencies ++= Seq(
      "org.http4s"         %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s"         %% "http4s-dsl"          % Http4sVersion,
      "org.http4s"         %% "http4s-twirl"        % Http4sVersion,
      "co.fs2"             %% "fs2-io"              % fs2Version,
      "ch.qos.logback"     %  "logback-classic"     % LogbackVersion,
      "com.github.poslegm" %% "scala-phash"         % scalaPhashVersion,
      compilerPlugin("com.olegpy" %% "better-monadic-for" % "0.2.4"),
      compilerPlugin("org.spire-math" %% "kind-projector" % "0.9.6")
    )
  )
