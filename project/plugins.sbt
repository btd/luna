libraryDependencies <+= sbtVersion(v => "com.github.siasia" %% "xsbt-web-plugin" % (v+"-0.2.10"))

resolvers += "my" at "http://btd.github.com/maven2"

addSbtPlugin("com.github.btd" % "sbt-less-plugin" % "0.0.1")