ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

val Http4sVersion = "1.0.0-M21"
val DoobieVersion = "1.0.0-M5"
val H2Version = "2.1.214"
val FlywayVersion = "9.2.0"
val CirceVersion = "0.14.1"
val PureConfigVersion = "0.17.1"
val LogbackVersion = "1.2.11"
val ScalaTestVersion = "3.2.13"
val ScalaMockVersion = "5.2.0"

lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(
    Defaults.itSettings,
    name := "Http4sDemo",
    idePackagePrefix := Some("io.sure360"),
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s" %% "http4s-circe" % Http4sVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "io.circe" %% "circe-generic" % CirceVersion,
      "org.tpolecat" %% "doobie-core" % DoobieVersion,
      "org.tpolecat" %% "doobie-h2" % DoobieVersion,
      "org.tpolecat" %% "doobie-hikari" % DoobieVersion,

      "com.h2database" % "h2" % H2Version,

      "org.flywaydb" % "flyway-core" % FlywayVersion,

      "io.circe" %% "circe-generic" % CirceVersion,

      "com.github.pureconfig" %% "pureconfig" % PureConfigVersion,
      "com.github.pureconfig" %% "pureconfig-cats-effect" % PureConfigVersion,

      "ch.qos.logback" % "logback-classic" % LogbackVersion,

      //Test libs
      "io.circe" %% "circe-literal" % CirceVersion % "it,test",
      "io.circe" %% "circe-optics" % CirceVersion % "it",
      "org.http4s" %% "http4s-blaze-client" % Http4sVersion % "it,test",
      "org.scalatest" %% "scalatest" % ScalaTestVersion % "it,test",
      "org.scalamock" %% "scalamock" % ScalaMockVersion % "test"
    )
  )
