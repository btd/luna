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

import bootstrap.liftweb._
import net.liftweb._
import http._
import common._
import util._
import Helpers._
import xml._
import Utility._
import code.model._
import code.lib._
import SnippetHelper._

import Sitemap._

/**
 * User: denis.bardadym
 * Date: 10/24/11
 * Time: 2:43 PM
 */

class PullRequestOps(repo: RepositoryDoc) extends Loggable {

  private var sourceRepo: RepositoryDoc = null

  private var sourceRef = ""

  private var destRepo: RepositoryDoc = null

  private var destRef = ""

  private var description = ""

  def renderUserClones =
    if (!repo.forkOf.valueBox.isEmpty) {
        ".repo_selector" #> SHtml.selectObj[RepositoryDoc](repo.owner.repos.filter(r => r.forkOf.get == repo.forkOf.get).map(r => r -> r.name.get),
          Full(repo), (r: RepositoryDoc) => {sourceRepo = r}, "class" -> "selectmenu repo_selector") &
          "name=srcRef" #> SHtml.text(sourceRef, s => {
            sourceRef = s.trim  
            if (sourceRef.isEmpty) S.error("Source reference is empty")
          }, "class" -> "textfield")
    } else {
        cleanAll
    }

  

  def renderAllClones = 
    if (!repo.forkOf.valueBox.isEmpty) {
      ".repo_selector" #> SHtml.selectObj[RepositoryDoc]((repo.forkOf.obj.get -> (repo.forkOf.obj.get.owner.login + "/" + repo.forkOf.obj.get.name.get)) :: RepositoryDoc.allClonesExceptOwner(repo).map(r => r -> (r.owner.login + "/" + r.name.get)),
        repo.forkOf.obj, (r: RepositoryDoc) => {
          destRepo = r
        }, "class" -> "selectmenu repo_selector") &
        "name=destRef" #> SHtml.text(destRef, s => {
          destRef = s.trim
          if (destRef.isEmpty) S.error("Destination reference is empty")
        }, "class" -> "textfield")
    } else {
      cleanAll
    } 

  def renderForm = w(UserDoc.currentUser){u => 
    if (!repo.forkOf.valueBox.isEmpty) {
      "button" #> SHtml.button(Text("new pull request"), processNewPullRequest(u), "class" -> "button", "id" -> "create_new_pull_request_button") &
        "name=description" #> SHtml.textarea(description, {
          value: String =>
            description = value.trim
        }, "placeholder" -> "Add a short description",
        "class" -> "textfield",
        "cols" -> "40", "rows" -> "20")
    } else {
      cleanAll
    }

  } 
  


  def processNewPullRequest(u : UserDoc)() = {
    val destHistory = destRepo.git.log(destRef).toList.reverse
    val srcHistory = sourceRepo.git.log(sourceRef).toList.reverse

    val diff = srcHistory.diff(destHistory)

    if(diff.isEmpty) {
      S.error("No new commits for destination repository")
    } else {
      PullRequestDoc.srcRepoId(sourceRepo.id.get)
          .destRepoId(destRepo.id.get)
          .srcRef(sourceRef)
          .destRef(destRef)
          .creatorId(u.id.get).description(description).save
        S.redirectTo(pullRequests.calcHref(destRepo))
    }
  }


  def renderPullRequests = 
   if(repo.pullRequests.isEmpty) 
    ".pull_request_list" #> "No pull requests for this repository."
   else
    ".pull_request" #> repo.pullRequests.map(pr => {
       ".pull_request [class+]" #> (if(pr.accepted_?.get) "closed_pr" else "opened_pr" ) &
        ".from" #> a(treeAtCommit.calcHref(SourceElement.rootAt(pr.srcRepoId.obj.get, pr.srcRef.get)), 
            Text(pr.srcRepoId.obj.get.owner.login.get + "/" + pr.srcRepoId.obj.get.name.get + "@" + pr.srcRef)) &
        ".to" #> a(treeAtCommit.calcHref(SourceElement.rootAt(pr.destRepoId.obj.get, pr.srcRef.get)), 
            Text(pr.destRepoId.obj.get.owner.login.get + "/" + pr.destRepoId.obj.get.name.get + "@" + pr.destRef)) &
        ".whom" #> a(userRepos.calcHref(pr.creatorId.obj.get), Text(pr.creatorId.obj.get.login.get)) &
        ".when" #> dateFormat(pr.creationDate.get) &
        ".msg" #> a(pullRequest.calcHref(pr), Text(if(!pr.description.get.isEmpty) escape(pr.description.get) else "No description"))
    })
  

}