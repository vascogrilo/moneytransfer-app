name := """vgrilo-moneytransfer"""

version := "0.1"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.11"

// Set JS Engine to use
//JsEngineKeys.engineType := JsEngineKeys.EngineType.Node

//libraryDependencies += javaJdbc
//libraryDependencies += cache
libraryDependencies += javaWs
// https://mvnrepository.com/artifact/org.json/json
libraryDependencies += "org.json" % "json" % "20170516"
// https://mvnrepository.com/artifact/org.hibernate.validator/hibernate-validator
libraryDependencies += "org.hibernate.validator" % "hibernate-validator" % "6.0.2.Final"


mainClass in assembly := Some("play.core.server.ProdServerStart")
assemblyJarName := "vgrilo-moneytransfer.jar"

val meta = """META.INF(.)*""".r
assemblyMergeStrategy in assembly := {
  case PathList("javax", "servlet", xs @ _*) => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".html" => MergeStrategy.first
  case n if n.startsWith("reference.conf") => MergeStrategy.concat
  case n if n.endsWith(".conf") => MergeStrategy.concat
  case meta(_) => MergeStrategy.discard
  case x => MergeStrategy.first
}