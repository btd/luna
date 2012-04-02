libraryDependencies <+= sbtVersion(v => "com.github.siasia" %% "xsbt-web-plugin" % (v+"-0.2.10"))

resolvers += Resolver.url("sbt-plugin-releases", url("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.untyped" % "sbt-js" % "0.3")

addSbtPlugin("com.untyped" % "sbt-less" % "0.3")

addSbtPlugin("com.untyped" % "sbt-runmode" % "0.3")