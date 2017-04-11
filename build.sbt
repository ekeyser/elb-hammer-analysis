import AssemblyKeys._

seq(assemblySettings: _*)
//assemblySettings

name := "elb-hammer-analysis"

version := "1.0"

scalaVersion := "2.12.1"

organization := "com.cloudywaters"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "2.1.0" % "provided",
  "org.apache.spark" %% "spark-sql" % "2.1.0" % "provided",
  "org.json4s" %% "json4s-native" % "3.5.0",
  "com.amazonaws" % "aws-java-sdk" % "1.11.97"
)

mergeStrategy in assembly := {
  case PathList("META-INF", xs@_*) => MergeStrategy.discard
  case x => MergeStrategy.first
}