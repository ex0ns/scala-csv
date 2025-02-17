import scala.sys.process._

Global / onChangedBuildSource := ReloadOnSourceChanges


val enableScalameter = settingKey[Boolean]("")

val sharedSettings = Seq(
  name := "scala-csv",
  version := "1.3.11-SNAPSHOT",
  organization := "com.github.tototoshi",
  TaskKey[Unit]("checkScalariform") := {
    val diff = "git diff".!!
    if(diff.nonEmpty){
      sys.error("Working directory is dirty!\n" + diff)
    }
  },
)

val testSettings = Seq(
  Test / parallelExecution := false,
  logBuffered := false,
  Test / publishArtifact := false,
  libraryDependencies ++= {
    Seq(
      "org.scalatest" %%% "scalatest" % "3.2.14" % Test,
      if (scalaVersion.value.startsWith("2.") && crossProjectPlatform.value.identifier != "native") "org.scalacheck" %%% "scalacheck" % "1.14.3" % Test
      else "org.scalacheck" %%% "scalacheck" % "1.17.0" % Test
    )
  },
)


val javaSettings = Seq(
  Compile / javaSource := baseDirectory.value / ".." / "src" / "main" / "java",
  compile / javacOptions += "-Xlint",
  compile / javacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, v)) if v <= 11 =>
        Seq("-target", "6", "-source", "6")
      case _ =>
        if (scala.util.Properties.isJavaAtLeast("9")) {
          // if Java9
          Nil
        } else {
          Seq("-target", "8")
        }
    }
  }
)

val scalaMeterSettings = Seq(
  enableScalameter := {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, v)) =>
        11 <= v && v <= 13
      case _ =>
        false
    }
  },
  Test / sources := {
    val s = (Test / sources).value
    val exclude = Set("CsvBenchmark.scala")
    if (enableScalameter.value) {
      s
    } else {
      s.filterNot(f => exclude(f.getName))
    }
  },
  testFrameworks += new TestFramework(
    "org.scalameter.ScalaMeterFramework"
  ),

  libraryDependencies ++= {
    if (enableScalameter.value) {
      Seq("com.storm-enroute" %% "scalameter" % "0.19" % "test")
    } else {
      Nil
    }
  },
)

lazy val publishSettings = Seq(
  publishMavenStyle := true,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (version.value.trim.endsWith("SNAPSHOT"))
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },
  pomExtra := <url>http://github.com/tototoshi/scala-csv</url>
  <licenses>
    <license>
      <name>Apache License, Version 2.0</name>
      <url>http://www.apache.org/licenses/LICENSE-2.0.html</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:tototoshi/scala-csv.git</url>
    <connection>scm:git:git@github.com:tototoshi/scala-csv.git</connection>
  </scm>
  <developers>
    <developer>
      <id>tototoshi</id>
      <name>Toshiyuki Takahashi</name>
      <url>http://tototoshi.github.com</url>
    </developer>
  </developers>
)


lazy val jvmSettings = Seq(
  scalaVersion := "2.13.10",
  crossScalaVersions := Seq("2.12.17", "2.11.12", "2.10.7", "2.13.10", "3.2.1"),

  scalacOptions ++= Seq(
    "-deprecation",
    "-feature",
    "-language:implicitConversions"
  ),

  scalacOptions ++= PartialFunction.condOpt(CrossVersion.partialVersion(scalaVersion.value)){
    case Some((2, v)) if v >= 11 => Seq("-Ywarn-unused")
  }.toList.flatten,

  initialCommands := """
                      |import com.github.tototoshi.csv._
                    """.stripMargin,


  Compile / unmanagedSourceDirectories += {
    val dir = CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, v)) if v <= 12 =>
        "scala-2.13-"
      case _ =>
        "scala-2.13+"
    }
    baseDirectory.value / ".." / "src" / "main" / dir
  }
)

lazy val nativeSettings = Seq(
  crossScalaVersions := Seq("2.12.15", "2.13.8", "3.1.0"),
  Compile / unmanagedSourceDirectories += {
    val dir = CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, v)) if v <= 12 =>
        "scala-2.13-"
      case _ =>
        "scala-2.13+"
    }
    baseDirectory.value / ".." / "src" / "main" / dir
  },
  Test / sources := {
    val s = (Test / sources).value
    val exclude = Set("CsvBenchmark.scala")
    s.filterNot(f => exclude(f.getName))
  },
)

lazy val `scala-csv` =
  // select supported platforms
  crossProject(JVMPlatform, NativePlatform)
    .crossType(CrossType.Pure)
    .in(file("."))
    .settings(sharedSettings)
    .jvmSettings(jvmSettings  ++ javaSettings ++ scalaMeterSettings ++ publishSettings ++ testSettings)
    .nativeSettings(nativeSettings ++ testSettings)
