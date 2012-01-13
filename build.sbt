name := "luna-tool"

version := "0.8-SNAPSHOT"

seq(webSettings :_*)

scalaVersion := "2.9.1"

scalacOptions += "-deprecation"

checksums := Nil

resolvers ++= Seq (
  "jgit-repository" at "http://download.eclipse.org/jgit/maven",
  "scala-tools snapshots" at "http://scala-tools.org/repo-snapshots",
  "my github repo" at "http://btd.github.com/maven2",
  "sbt-idea-repo" at "http://mpeltonen.github.com/maven/",
  "Web plugin repo" at "http://siasia.github.com/maven2",
  Resolver.url("Typesafe repository", new java.net.URL("http://typesafe.artifactoryonline.com/typesafe/ivy-releases/"))(Resolver.defaultIvyPatterns)
)

libraryDependencies <+= sbtVersion(v => "com.github.siasia" %% "xsbt-web-plugin" % (v + "-0.2.10"))

libraryDependencies ++= Seq (
  "net.databinder"      %% "dispatch-nio"         % "0.8.7",
  "org.mindrot"         % "jbcrypt"               % "0.3m",
  "org.apache.sshd"     % "sshd-core" 	          % "0.6.0",
  "org.eclipse.jgit"    % "org.eclipse.jgit"      % "1.1.0.201109151100-r",
  "org.apache.mina"     % "mina-core"             % "2.0.4",
  "commons-codec"       % "commons-codec"         % "1.5",
  "org.lunatool"        %% "scala-linguist"       % "1.3",
  "junit"               % "junit"                 % "4.8"                   % "test",
  "net.liftweb"         %% "lift-webkit"          % "2.4-SNAPSHOT"          % "compile",
  "net.liftweb"         %% "lift-db"              % "2.4-SNAPSHOT"          % "compile",
  "net.liftweb"         %% "lift-mongodb"         % "2.4-SNAPSHOT"          % "compile",
  "net.liftweb"         %% "lift-mongodb-record"  % "2.4-SNAPSHOT"          % "compile",
  "com.foursquare"      %% "rogue"                % "1.0.29"                intransitive(),
  "org.eclipse.jetty"   % "jetty-webapp"          % "8.0.1.v20110908"       % "container",
  "com.github.siasia"   %% "xsbt-web-plugin"      % "0.2.10",
  "ch.qos.logback"      % "logback-classic"       % "1.0.0"
)
