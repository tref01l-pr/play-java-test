name := """play-java-seed"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.13.15"

libraryDependencies += guice
libraryDependencies += ws
libraryDependencies += "com.auth0" % "java-jwt" % "4.4.0"
libraryDependencies += "org.mongodb" % "mongodb-driver-sync" % "4.9.1"
libraryDependencies += "dev.morphia.morphia" % "morphia-core" % "2.4.14"
libraryDependencies += "commons-codec" % "commons-codec" % "1.17.1"
libraryDependencies += "org.bouncycastle" % "bcprov-jdk15on" % "1.70"