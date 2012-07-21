libraryDependencies <+= sbtVersion(v => v match {
case "0.11.2" => "com.github.siasia" %% "xsbt-web-plugin" % "0.11.2-0.2.11"
case "0.11.3" => "com.github.siasia" %% "xsbt-web-plugin" % "0.11.3-0.2.11.1"
})

resolvers += "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases"

addSbtPlugin("com.github.btd" % "sbt-style-plugin"    % "0.0.3")