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
package code.rest

import net.liftweb._
import http._
import common._
import util._
import json._
import json.JsonDSL._

import code.model._
import xml._
import Utility._
import com.foursquare.rogue.Rogue._
import net.liftweb.http.rest._

object ApiV1 extends Loggable with RestHelper {
  import Helpers._

  def loginSuccessResponce(user: UserDoc) = {
    val token = Session.put(SessionData(Some(user)))
    JsonResponse(JObject(JField("access_token", token) :: JField("user", user.asJValue) :: Nil))
  }

  serve { "api" / "1" prefix {
    case "auth" :: "token" :: Nil JsonGet _ => {
      S.param("access_token") match {
        case Full(token) => 
          Session.get(token).user match {
            case None => ForbiddenResponse("There is no any session for this token")
            case Some(user) => JsonResponse(JObject(JField("access_token", token) :: JField("user", user.asJValue) :: Nil))
          }
        case _ => 
          (S.param("login"), S.param("password")) match {
            case (Full(login), Full(password)) => 
              UserDoc.byName(login) match {
                case Some(user) if user.password.match_?(password) => loginSuccessResponce(user)
                case Some(user) => ForbiddenResponse("Wrong password.")
                case _ => 
                  val user = UserDoc.createRecord.login(login).password(password)
                  user.validate match {
                    case Nil => 
                      user.save 
                      loginSuccessResponce(user)
                    case l => ForbiddenResponse(l.map(_.msg).mkString(". "))
                  }
                
              }
            case _ => BadResponse()
          }
      }
      
    }
    case "wiki" :: "root" :: Nil JsonGet _ => JsonResponse(JObject(JField("content", code.snippet.WelcomeWiki.finalContent) :: Nil))

    case "user" :: id :: "repositories" :: Nil JsonGet _ => BadResponse()
  }}


}

trait Init {
  
  def init: Unit

  def isInited: Boolean

}

object H { 
  def tryo[A](f: => A): Option[A]  = 
    try {
      Some(f)
    } catch {
      case _ => None
    }
}

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

case class SessionData(user: Option[UserDoc])

object Session extends Init {
  import com.google.common.cache._
  import java.util.concurrent._

  def newToken = java.util.UUID.randomUUID.toString

  private var loader = new CacheLoader[String, SessionData] {
    def load(token: String) = SessionData(None)
  }

  private var cache: Option[LoadingCache[String, SessionData]] = None

  def init {
    val ttl = P.sessionLifeTime

    cache = Some(CacheBuilder.from("expireAfterWrite=" + ttl)
                              .build(loader))
  }
  def isInited = !cache.isEmpty

  def put(data: SessionData): Option[String] = {
    for {
      c <- cache
      token = newToken
    } yield {
      c.put(token, data)
      token
    }
  }

  def get(token: String) = cache.get(token)
}


/**
 It is for storing and accessing properties
*/
object P extends net.liftweb.common.Loggable   {
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

   def workingDir = propStr(workingDirProperty)

   val welcomePageProperty = ("luna.working_dir", () => workingDir / "welcome.md")

   val sessionLifeTimeProperty = ("luna.session_life_time", () => "10m")

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