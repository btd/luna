name := "luna-tool"

version := "1.1-SNAPSHOT"

seq(webSettings :_*)

seq(lessSettings : _*)

scalaVersion := "2.9.1"

scalacOptions += "-deprecation"

resolvers ++= Seq (
  "jgit-repository" at "http://download.eclipse.org/jgit/maven",
  "Sonatype Snapshot" at "https://oss.sonatype.org/content/repositories/snapshots",
  "my github repo" at "http://btd.github.com/maven2"
  )


libraryDependencies ++= {
  val liftVersion = "2.5-M4"
  Seq (
  "net.databinder"      %% "dispatch-nio"                 % "0.8.9",
  "org.mindrot"         % "jbcrypt"                       % "0.3m",
  "org.eclipse.jgit"    % "org.eclipse.jgit"              % "2.2.0.201212191850-r",
  "org.apache.sshd"     % "sshd-core"                     % "0.8.0",
  "org.apache.mina"     % "mina-core"                     % "2.0.7",
  "commons-codec"       % "commons-codec"                 % "1.7",
  "org.lunatool"        %% "scala-linguist"               % "1.3",
  "org.quartz-scheduler" % "quartz"                       % "2.1.6",
  "commons-io"          % "commons-io"                    % "2.4",
  "javax.transaction"   % "jta"                           % "1.1"                    % "container",
  "net.liftweb"         %% "lift-webkit"                  % liftVersion              % "compile",
  "net.liftweb"         %% "lift-db"                      % liftVersion              % "compile",
  "net.liftweb"         %% "lift-mongodb"                 % liftVersion              % "compile",
  "net.liftweb"         %% "lift-mongodb-record"          % liftVersion              % "compile",
  "com.foursquare"      %% "rogue"                        % "1.1.6"                  intransitive(),
  "org.eclipse.jetty"   % "jetty-webapp"                  % "8.0.4.v20111024"        % "container",
  "ch.qos.logback"      % "logback-classic"               % "1.0.9"
  )
}

(resourceManaged in (Compile, LessKeys.less)) <<= (sourceDirectory in Compile)(_ / "webapp" / "assets")
