/*
 * Copyright (c) 2011 Denis Bardadym
 * This project like github.
 * Distributed under Apache Licence.
 */

name := "GitoChtoTo"

version := "0.1-SNAPHOT"

scalaVersion := "2.8.1"

resolvers += "jgit-repository" at "http://download.eclipse.org/jgit/maven"

libraryDependencies ++= Seq (
	"org.apache.sshd" 	% "sshd-core" 	      % "0.5.0"			,
  "org.eclipse.jgit"  % "org.eclipse.jgit"  % "1.0.0.201106090707-r",
  "org.slf4j"         % "slf4j-jdk14"       % "1.5.11"     ,
  "com.twitter"       % "util-core"         % "1.10.4",
  "commons-dbcp"      % "commons-dbcp"      % "1.4",
  "commons-pool"      % "commons-pool"      % "1.5.4",
	"junit" 			      % "junit" 		        % "4.8" 	% "test"
	)