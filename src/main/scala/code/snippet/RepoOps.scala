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
package code.snippet

import net.liftweb._
import common._
import http._
import util.Helpers._
import SnippetHelper._
import bootstrap.liftweb._
import util._
import xml._
import js._
import code.model._

/**
 * User: denis.bardadym
 * Date: 10/5/11
 * Time: 2:14 PM
 */

class RepoOps(urp: WithRepo) extends Loggable {

  def renderSourceTreeDefaultLink = w(urp.repo)(renderSourceTreeLink(_, None))

  def renderCommitsDefaultLink = w(urp.repo)(renderCommitsLink(_, None))

  def renderPullRequestsLink = w(urp.repo){repo => {
    val pullRequestCount = repo.pullRequests.filter(!_.accepted_?.get).size
    def text = {
      if(pullRequestCount == 0) Text("Pull requests")
      else Text("Pull requests (%d)" format pullRequestCount)
    }
    ".repo_menu_link *" #> (S.attr("current") match {
      case Full(_) => text
      case _ => a(repo.pullRequestsUrl, text)
    })
  }} 

  def notifyEmailForm = w(UserDoc.currentUser){ u => w(urp.repo) { r =>
    import notification.client._
    import com.foursquare.rogue.Rogue._

    val settings = (NotifySubscriptionDoc where (_.who eqs u.id.get) and (_.repo eqs r.id.get) and (_.onWhat eqs NotifyEvents.Push) get) 
        .getOrElse(NotifySubscriptionDoc.createRecord.who(u.id.get).repo(r.id.get).onWhat(NotifyEvents.Push))
    emailForm(settings.output.get.email.get, u) &
    "button" #> SHtml.button("Update settings", saveNotifySettings(settings) _)
  }}

  private def saveNotifySettings(s : NotifySubscriptionDoc)() {
    s.save
  }

  private def emailForm(emailOutput: Email, user: UserDoc): CssSel = {
    var emails:List[String] = emailOutput.to.get

    if(emails.isEmpty) emails = user.email.get :: Nil

    "name=emails" #> SHtml.text(emails.mkString("; "), v => emailOutput.to(v.split(";").map(_.trim).toList),
      "class" -> "textfield large") &
    "name=activated" #> SHtml.checkbox(emailOutput.activated.get, v => emailOutput.activated(v))
  }

  def renderRepositoryBlock = w(urp.user){u =>  w(urp.repo){repo => 
      ".repo [class+]" #> (UserDoc.currentUser match {
        case Full(cu) if(!repo.owner_?(Full(cu)) && repo.canPush_?(Full(cu))) => "collaborated"
        case Full(cu) if(repo.owner_?(Full(cu)))=> if(repo.open_?.get) "public" else "private"
        case _ => "public"
      }) &
      ".repo *" #> (
          ".repo_name *" #> <span><a href={repo.owner.homePageUrl}>{repo.owner.login.get}</a>/{repo.name.get}</span> &
          ".clone-url *" #> (repo.cloneUrlsForCurrentUser.map(url => "a" #> a(url._1, Text(url._2)))) &
        (UserDoc.currentUser match {
                case Full(cu) if (cu.login.get == u.login.get) => {
                    
                    ".admin_page *" #> a("/admin" + repo.homePageUrl, Text("admin")) & 
                    ".fork *" #> SHtml.a(makeFork(repo, cu) _, Text("fork it")) &
                    ".notification_page *" #> a(repo.homePageUrl + "/notify", Text("notify")) &
                    ".toggle_open *" #> SHtml.a(toggleOpen(repo) _, Text(if (repo.open_?.get) "make private" else "make public")) &
                    (repo.forkOf.obj.map(fr => ".origin_link *" #> a(fr.sourceTreeUrl, Text("origin"))) openOr 
                          ".origin_link" #> NodeSeq.Empty)
                }
                case Full(cu) => {     
                    ".admin_page" #> NodeSeq.Empty & 
                    ".fork *" #> SHtml.a(makeFork(repo, cu) _, Text("fork it")) &
                    ".toggle_open" #> NodeSeq.Empty &
                    ".notification_page *" #> a(repo.homePageUrl + "/notify", Text("notify")) &
                    (repo.forkOf.obj.map(fr => ".origin_link *" #> a(fr.sourceTreeUrl, Text("origin"))) openOr 
                          ".origin_link" #> NodeSeq.Empty)
                  }
                case _ => {
                    (repo.forkOf.obj match {
                          case Full(fr) => {
                            ".origin_link *" #> a(fr.sourceTreeUrl, Text("origin")) &
                            ".admin_page" #> NodeSeq.Empty & 
                            ".toggle_open" #> NodeSeq.Empty &
                            ".fork" #> NodeSeq.Empty &
                            ".notification_page" #> NodeSeq.Empty
                          }
                          case _ =>  ".admin" #> NodeSeq.Empty 
                        })
                }
            }))
  }} 

  

  
}