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
package bootstrap.liftweb

import net.liftweb._
import common._
import util._
import http._
import mongodb.{DefaultMongoIdentifier, MongoDB}
import http.auth._

import http.js.JE._
import http.js.jquery._
import http.js.{JsCmd, JsExp, JsMember}
import JqJE._
import JqJsCmds._

import sitemap.SiteMap

import util.Helpers._

import xml.{NodeSeq, Text}

import daemon.git.GitDaemon
import daemon.sshd.SshDaemon

import com.mongodb.Mongo

import code.model._

import code.lib.Sitemap


import main.Constants._


case class JqShow() extends JsExp with JsMember {
   def toJsCmd = "show()"
}

case class JqHide() extends JsExp with JsMember {
   def toJsCmd = "hide()"
}


/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot extends Loggable {
  def boot {
    val dbHost = Props.get("db.host", "localhost")
    val dbPort = Props.getInt("db.port", 27017)
    val dbName = Props.get("db.name", "grt")

    Props.get("db.user") match {
      case Full(userName) => {
        Props.requireOrDie("db.password")
        MongoDB.defineDbAuth(DefaultMongoIdentifier, new Mongo(dbHost, dbPort), dbName, userName, Props.get("db.password").get)
      }
      case _ => 
        MongoDB.defineDb(DefaultMongoIdentifier, new Mongo(dbHost, dbPort), dbName)
    }

    try { 
      SshDaemon.init
    } catch {
      case e => logger.warn("Exception while start sshd", e)
    }
    try { 
      GitDaemon.init
    } catch {
      case e => logger.warn("Exception while start gitd", e)
    }

    LiftRules.unloadHooks.append(
      () => {
        SshDaemon.shutdown
        GitDaemon.shutdown
        notification.client.NotifyActor.onShutdown
      }
    )

    // where to search snippet
    LiftRules.addToPackages("code")

    LiftRules.explicitlyParsedSuffixes = Set()

    //not so good but enough
    if(Props.getBool(USER_REGISTRATION_ENABLED, true)) {

      LiftRules.setSiteMap(SiteMap(Sitemap.entries: _*))
      
      LiftRules.statelessRewrite.append {
        case RewriteRequest(ParsePath("index" :: Nil, _, _, true), _, _) =>

          RewriteResponse("user" :: "m" :: "signin" :: Nil, true)
      }

    } else {
      if(!UserDoc.adminExists_?) {
        UserDoc.addDefaultAdmin
      }
      LiftRules.setSiteMap(SiteMap(Sitemap.defaultEntries: _*))
      
      LiftRules.statelessRewrite.append {
        case RewriteRequest(ParsePath("index" :: Nil, _, _, true), _, _) =>

          RewriteResponse("user" :: "m" :: "login" :: Nil, true)
      }
    }
    

    LiftRules.dispatch.append(code.snippet.RawFileStreamingSnippet)
    LiftRules.dispatch.append(code.snippet.GitHttpSnippet)

    LiftRules.ajaxRetryCount = Full(1)
    LiftRules.ajaxPostTimeout = 15000

    



    def open_?(userName: String, repoName: String):Boolean = {
      RepositoryDoc.byUserLoginAndRepoName(userName, repoName.substring(0, repoName.length - 4)) match {
        case Some(r) => r.open_?.get
        case _ => false
      }
    }

    LiftRules.httpAuthProtectedResource.prepend{ 
      case Req(userName :: repoName :: "info" :: "refs" :: Nil, _, _) 
        if(repoName.endsWith(".git") && 
          !S.param("service").isEmpty && (
              S.param("service").get == "git-receive-pack" || 
              !open_?(userName, repoName))) => Empty
      case Req(userName :: repoName :: "git-receive-pack" :: Nil, _, PostRequest) 
        if(repoName.endsWith(".git")) => Empty
      case Req(userName :: repoName :: "git-upload-pack" :: Nil, _, PostRequest) 
        if(repoName.endsWith(".git") && !open_?(userName, repoName)) => Empty 
    } 

    LiftRules.authentication = HttpBasicAuthentication("lift") { 
      case (username, password, Req(userName :: repoName :: _, _, _)) if repoName.endsWith(".git") => { 
        UserDoc.byName(username) match {
          case Some(user) if user.suspended.get => false

          case Some(user) if user.password.match_?(password) => 
            RepositoryDoc.byUserLoginAndRepoName(userName, repoName.substring(0, repoName.length - 4)) match {
              case Some(r) => r.canPush_?(Some(user))
              case _ => false
            }
          case _ => false 
        } 
      } 
    }

    LiftRules.liftRequest.append {
      case Req("assets" :: Nil, _, _)  => false
    }

    LiftRules.ajaxStart = Full(() => JqId("preloader") ~> JqShow())

    LiftRules.ajaxEnd = Full(() => JqId("preloader") ~> JqHide())

    // Use jQuery 1.4
    //LiftRules.jsArtifacts = net.liftweb.http.js.jquery.JQuery14Artifacts

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    LiftRules.htmlProperties.default.set((r: Req) => new Html5Properties(r.userAgent))

  }

}
