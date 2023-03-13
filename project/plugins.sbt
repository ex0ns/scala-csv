addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.8.3")

addSbtPlugin("com.github.tkawachi" % "sbt-doctest" % "0.10.0")

addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.9.14")

addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.2.0")

libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % "always"

addSbtPlugin("org.scala-native"   % "sbt-scala-native"              % "0.4.10")

addSbtPlugin("org.portable-scala" % "sbt-scala-native-crossproject" % "1.2.0")