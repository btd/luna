libraryDependencies <+= sbtVersion(v => v match {
case "0.11.2" => "com.github.siasia" %% "xsbt-web-plugin" % "0.11.2-0.2.11"
case "0.11.3" => "com.github.siasia" %% "xsbt-web-plugin" % "0.11.3-0.2.11.1"
})

addSbtPlugin("com.github.btd" % "sbt-style-plugin"    % "0.0.3")

resolvers ++= Seq (
  "Sonatype Release" at "http://oss.sonatype.org/content/repositories/releases",
  "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots"
  )