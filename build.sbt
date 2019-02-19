val Http4sVersion = "0.19.0"
val LogbackVersion = "1.2.3"
val scalaPhashVersion = "1.2.0"
val fs2Version = "1.0.3"
val PureConfigVersion = "0.10.2"

lazy val root = (project in file("."))
  .enablePlugins(SbtTwirl)
  .settings(
    organization := "com.github.poslegm",
    name := "scalaphashdemo",
    version := "0.0.2",
    scalaVersion := "2.12.8",
    scalafmtOnCompile := true,
    scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-feature",
      "-language:higherKinds",
      "-Xfatal-warnings",
      "-Ywarn-unused:params,locals,privates,patvars",
      "-Ypartial-unification"
    ),
    libraryDependencies ++= Seq(
      "org.http4s"            %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s"            %% "http4s-dsl"          % Http4sVersion,
      "org.http4s"            %% "http4s-twirl"        % Http4sVersion,
      "co.fs2"                %% "fs2-io"              % fs2Version,
      "ch.qos.logback"        %  "logback-classic"     % LogbackVersion,
      "com.github.poslegm"    %% "scala-phash"         % scalaPhashVersion,
      "com.github.pureconfig" %% "pureconfig"          % PureConfigVersion,
      "io.chrisdavenport"     %% "cats-par"            % "0.2.1",
      
      compilerPlugin("com.olegpy"     %% "better-monadic-for" % "0.3.0-M4"),
      compilerPlugin("org.spire-math" %% "kind-projector"     % "0.9.9")
    )
  )

