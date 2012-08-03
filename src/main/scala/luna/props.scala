package luna.props

import luna.help._

object Config extends Init {
  import com.typesafe.config._
  import H._

  private var config: Option[Config] = None

  def init {
    config = Some(ConfigFactory.load)
  }
  def isInited = !config.isEmpty

  def getString(key: String) = config.flatMap(c => tryo(c.getString(key)))
  def getString(key: String, default: String) = config.flatMap(c => tryo(c.getString(key))).getOrElse(default)
}

object P extends Loggable   {
   object Path {
      val userHome = new Path(System.getProperty("user.home"))
   }
   class Path(val path: String) {
      def /(str: String) = new Path(path + java.io.File.separator + str)

      override def toString = path
   }


   implicit def strToPath(s: String): Path = new Path(s)

   implicit def PathToString(p: Path): String = p.path

   private def propStr(p: (String, () => Any)) = code.rest.Config.getString(p._1, p._2().toString)

   val workingDirProperty = ("luna.working_dir", () => Path.userHome / ".luna")
   val welcomePageProperty = ("luna.welcome_file", () => workingDir / "welcome.md")
   val sessionLifeTimeProperty = ("luna.session_life_time", () => "10m")
   val dbNameProperty = ("luna.db_name", () => "luna")
   val runModeProperty = ("luna.run_mode", () => "dev")

   def workingDir = propStr(workingDirProperty)

   def runMode = propStr(runModeProperty)

   def devMode = runMode == "dev"

   def dbName = propStr(dbNameProperty)

   def sessionLifeTime = propStr(sessionLifeTimeProperty)

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