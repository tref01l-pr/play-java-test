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

libraryDependencies += "org.apache.pdfbox" % "pdfbox" % "3.0.4"
libraryDependencies += "org.apache.pdfbox" % "pdfbox-tools" % "3.0.4"

libraryDependencies += "com.github.jai-imageio" % "jai-imageio-core" % "1.4.0"
libraryDependencies += "com.github.jai-imageio" % "jai-imageio-jpeg2000" % "1.4.0"
libraryDependencies += "io.github.cdimascio" % "dotenv-java" % "3.1.0"
libraryDependencies += "org.apache.tika" % "tika-core" % "3.1.0"

libraryDependencies += "org.ghost4j" % "ghost4j" % "1.0.1"

libraryDependencies ++= Seq(
  "com.itextpdf" % "itext7-core" % "9.0.0",
  "com.itextpdf" % "kernel" % "9.0.0",
  "com.itextpdf" % "io" % "9.0.0",
  "com.itextpdf" % "layout" % "9.0.0"
)