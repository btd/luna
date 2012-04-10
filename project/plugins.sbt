resolvers += "my" at "http://btd.github.com/maven2"

libraryDependencies <+= sbtVersion(v => "com.github.siasia" %% "xsbt-web-plugin" % (v+"-0.2.11"))

addSbtPlugin("com.github.btd" % "sbt-less-plugin" % "0.0.1")