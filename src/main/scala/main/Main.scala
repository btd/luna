/*
   Copyright 2012 Denis Bardadym

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package main

object Constants {
	val SSHD_PORT_OPTION = "daemon.sshd.port"

	val GITD_PORT_OPTION = "daemon.gitd.port"

	val REPOSITORIES_DIR = "repository.dir"

	val SSHD_CERT_PATH = "daemon.sshd.cert.path" 

	val SSHD_CERT_PATH_DEFAULT = "./"

   val USER_REGISTRATION_ENABLED = "users.registration.enabled" //true
}

/**
 It is for storing and accessing properties
*/
object P extends net.liftweb.common.Loggable    {
   object Path {
      val userHome = new Path(System.getProperty("user.home"))
   }
   class Path(val path: String) {
      def /(str: String) = new Path(path + java.io.File.separator + str)

      override def toString = path
   }


   implicit def strToPath(s: String): Path = new Path(s)

   implicit def PathToString(p: Path): String = p.path

   private def propStr(p: (String, () => Any)) = net.liftweb.util.Props.get(p._1, p._2().toString)

   val workingDirProperty = ("luna.working_dir", () => Path.userHome / ".luna")

   def workingDir = propStr(workingDirProperty)

   val welcomePageProperty = ("luna.working_dir", () => workingDir / "welcome.md")

   def welcomePage = propStr(welcomePageProperty)

   def init = {
      logger.debug("Initialization of Luna instance")
      logger.debug("User home: " + Path.userHome)

      logger.debug("Checking working dir: " + workingDir)
      val workingDirFile = new java.io.File(workingDir)
      if(!workingDirFile.exists) {
         logger.debug("Working dir doesn't exists, will create it")
         workingDirFile.mkdirs
      }

      logger.debug("Checking welcome file: " + welcomePage)
      val welcomeFile = new java.io.File(welcomePage)
      if(!welcomeFile.exists) {
         logger.debug("Welcome file doesn't exists. Create one, fill with markdown and reload page (it will be cached in production mode)")
      }
   }

}