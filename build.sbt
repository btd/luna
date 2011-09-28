name := "GitoChtoTo"

version := "0.3-SNAPSHOT"

seq(webSettings :_*)

scalaVersion := "2.9.1"

checksums := Nil

resolvers ++= Seq (
  "jgit-repository" at "http://download.eclipse.org/jgit/maven",
  "scala-tools snapshots" at "http://scala-tools.org/repo-snapshots"
  )


libraryDependencies ++= Seq (
  "org.apache.sshd"     % "sshd-core" 	      % "0.5.0",
  "org.eclipse.jgit"    % "org.eclipse.jgit"  % "1.0.0.201106090707-r",
  "commons-codec"       % "commons-codec"     % "1.5",
  "com.jolbox"          % "bonecp"            % "0.7.1.RELEASE",
  "com.h2database"      % "h2"                % "1.3.160",
  "junit"               % "junit"             % "4.8"                   % "test",
  "net.liftweb"         %% "lift-webkit"      % "2.4-SNAPSHOT"          % "compile",
  "net.liftweb"         %% "lift-db"          % "2.4-SNAPSHOT"          % "compile",
  "org.eclipse.jetty"   % "jetty-webapp"      % "8.0.0.v20110901"       % "jetty",
  "ch.qos.logback"      % "logback-classic"   % "0.9.26"
  )