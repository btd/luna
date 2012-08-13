name := "luna"

version := "2.0-SNAPSHOT"

seq(webSettings :_*)

seq(styleSettings : _*)

scalaVersion := "2.9.1"

scalacOptions += "-deprecation"

resolvers ++= Seq (
  "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository",
  "jgit-repository" at "http://download.eclipse.org/jgit/maven",
  "Sonatype Release" at "http://oss.sonatype.org/content/repositories/releases",
  "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
  "my github repo" at "http://btd.github.com/maven2"
  )


libraryDependencies ++= Seq (
  "com.novus"           %% "salat"                        % "1.9.0",
  "javax.servlet"       % "servlet-api"                   % "2.5" % "provided",
  "net.databinder"      %% "unfiltered"                   % "0.6.3",
  "net.databinder"      %% "unfiltered-filter"            % "0.6.3",
  "net.databinder"      %% "unfiltered-jetty"             % "0.6.3",
  "net.databinder"      %% "unfiltered-json"              % "0.6.3",
  "com.typesafe"        % "config"                        % "0.5.0",
  "com.google.guava"    % "guava"                         % "12.0",
  "org.pegdown"         % "pegdown"                       % "1.1.0",
  "net.databinder"      %% "dispatch-nio"                 % "0.8.7",
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
  "ch.qos.logback"      % "logback-classic"               % "1.0.6"                  
  )


(resourceManaged in (Compile, StyleKeys.less)) <<= (sourceDirectory in Compile)(_ / "webapp" / "assets")

(StyleKeys.minify in (Compile, StyleKeys.combine)) := true