name := "luna-tool"

version := "1.1-SNAPSHOT"

seq(webSettings :_*)

scalaVersion := "2.9.1"

scalacOptions += "-deprecation"

resolvers ++= Seq (
  "jgit-repository" at "http://download.eclipse.org/jgit/maven",
  "Sonatype Snapshot" at "https://oss.sonatype.org/content/repositories/snapshots",
  "my github repo" at "http://btd.github.com/maven2"
  )


libraryDependencies ++= Seq (
  "net.databinder"      %% "dispatch-nio"                 % "0.8.8",
  "org.mindrot"         % "jbcrypt"                       % "0.3m",
  "org.eclipse.jgit"    % "org.eclipse.jgit"              % "1.3.0.201202151440-r",
  "org.apache.sshd"     % "sshd-core"                     % "0.6.0",
  "org.apache.mina"     % "mina-core"                     % "2.0.4",
  "commons-codec"       % "commons-codec"                 % "1.5",
  "org.lunatool"        %% "scala-linguist"               % "1.3",
  "org.quartz-scheduler" % "quartz"                       % "2.1.3",
  "commons-io"          % "commons-io"                    % "2.1",
  "javax.transaction"   % "jta"                           % "1.1"                    % "container",
  "junit"               % "junit"                         % "4.8"                    % "test",
  "net.liftweb"         %% "lift-webkit"                  % "2.5-SNAPSHOT"           % "compile",
  "net.liftweb"         %% "lift-db"                      % "2.5-SNAPSHOT"           % "compile",
  "net.liftweb"         %% "lift-mongodb"                 % "2.5-SNAPSHOT"           % "compile",
  "net.liftweb"         %% "lift-mongodb-record"          % "2.5-SNAPSHOT"           % "compile",
  "com.foursquare"      %% "rogue"                        % "1.1.6"                  intransitive(),
  "org.eclipse.jetty"   % "jetty-webapp"                  % "8.0.4.v20111024"        % "container",
  "ch.qos.logback"      % "logback-classic"               % "1.0.0",
  "io.netty"            % "netty"                         % "3.3.1.Final"            % "compile"
  )

(resourceManaged in (Compile, LessKeys.less)) <<= (sourceDirectory in Compile)(_ / "webapp" / "assets")
