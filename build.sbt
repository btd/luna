name := "GitoChtoTo"

version := "0.1-SNAPHOT"

scalaVersion := "2.8.1"

resolvers += "jgit-repository" at "http://download.eclipse.org/jgit/maven"

libraryDependencies ++= Seq (
	"org.apache.sshd" 	% "sshd-core" 	% "0.5.0"			,
  "org.eclipse.jgit"  % "org.eclipse.jgit" % "1.0.0.201106090707-r",
  "org.slf4j"         % "slf4j-jdk14"   % "1.5.11"     ,
	"junit" 			      % "junit" 		  % "4.8" 	% "test"
	)