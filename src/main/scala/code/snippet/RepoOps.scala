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
import notification.client._
import code.lib.Sitemap._

/**
 * User: denis.bardadym
 * Date: 10/5/11
 * Time: 2:14 PM
 */

class RepoOps(repo: RepositoryDoc) extends Loggable {

  def renderSourceTreeDefaultLink = renderSourceTreeLink(repo, None)

  def renderCommitsDefaultLink = renderCommitsLink(repo, None)

  def renderPullRequestsDefaultLink: NodeSeq => NodeSeq = 
    renderPullRequestsLink(repo)

  def notifyEmailForm = w(UserDoc.currentUser){ u => 
    
    import com.foursquare.rogue.Rogue._

    val settings = (NotifySubscriptionDoc where (_.who eqs u.id.get) and (_.repo eqs repo.id.get) and (_.onWhat eqs NotifyEvents.Push) get) 
        .getOrElse(NotifySubscriptionDoc.createRecord.who(u.id.get).repo(repo.id.get).onWhat(NotifyEvents.Push))
    emailForm(settings.output.get.email.get, u) &
    "button" #> SHtml.button("Update settings", saveNotifySettings(settings) _)
  }

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

  def renderRepositoryBlockDefault = renderRepositoryBlock(repo, 
                          repo.owner, 
                          r => <span><a href={userRepos.calcHref(r.owner)}>{r.owner.login.get}</a>/{r.name.get}</span>)

  

  
}