resolvers ++= Seq("sbt-idea-repo" at "http://mpeltonen.github.com/maven/",
					"Web plugin repo" at "http://siasia.github.com/maven2")

libraryDependencies ++= Seq("com.github.mpeltonen" %% "sbt-idea" % "0.10.0",
						 "com.github.siasia" %% "xsbt-web-plugin" % "0.1.1-0.10.1")