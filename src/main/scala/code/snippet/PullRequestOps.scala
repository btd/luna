/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package code.snippet

import bootstrap.liftweb.RepoPage
import net.liftweb._
import http._
import common._
import util._
import Helpers._
import scala.xml._
import code.model._
import code.snippet.SnippetHelper._

/**
 * User: denis.bardadym
 * Date: 10/24/11
 * Time: 2:43 PM
 */

class PullRequestOps(urp: PullRequestRepoPage) extends Loggable {

  private var sourceRepo: RepositoryDoc = null

  private var sourceRef = ""

  private var destRepo: RepositoryDoc = null

  private var destRef = ""

  private var description = ""

  def renderUserClones = {
    urp.repo match {
      case Full(repo) => {
        ".repo_selector" #> SHtml.selectObj(urp.user.get.repos.filter(r => r.forkOf.get == repo.forkOf.get).map(r => r -> r.name.get),
          Full(repo), (r: RepositoryDoc) => {
            sourceRepo = r
          }, "class" -> "selectmenu repo_selector") &
          "name=srcRef" #> SHtml.text(sourceRef, s => {
            sourceRef = s.trim
            if (sourceRef.isEmpty) S.error("Source reference is empty")
          }, "class" -> "textfield")
      }
      case _ => "*" #> NodeSeq.Empty
    }
  }

  def renderAllClones = {
    urp.repo match {
      case Full(repo) if (!repo.forkOf.valueBox.isEmpty)=> {
        ".repo_selector" #> SHtml.selectObj[RepositoryDoc]((repo.forkOf.obj.get -> (repo.forkOf.obj.get.owner.login + "/" + repo.forkOf.obj.get.name.get)) :: RepositoryDoc.allClonesExceptOwner(repo).map(r => r -> (r.owner.login + "/" + r.name.get)),
          repo.forkOf.obj, (r: RepositoryDoc) => {
            destRepo = r
          }, "class" -> "selectmenu repo_selector") &
          "name=destRef" #> SHtml.text(destRef, s => {
            destRef = s.trim
            if (destRef.isEmpty) S.error("Destination reference is empty")
          }, "class" -> "textfield")
      }
      case _ => "*" #> NodeSeq.Empty
    }
  }

  def renderForm = {
    urp.repo match {
      case Full(repo) => {
        "button" #> SHtml.button(Text("new pull request"), processNewPullRequest, "class" -> "button", "id" -> "create_new_pull_request_button") &
          "name=description" #> SHtml.textarea(description, {
            value: String =>
              description = value.trim
          }, "placeholder" -> "Add a short description",
          "class" -> "textfield",
          "cols" -> "40", "rows" -> "20")
      }
      case _ => "*" #> NodeSeq.Empty
    }
  }


  def processNewPullRequest() = {
    UserDoc.currentUser match {
      case Full(u) => {
        PullRequestDoc.srcRepoId(sourceRepo.id.get)
          .destRepoId(destRepo.id.get)
          .srcRef(sourceRef)
          .destRef(destRef)
          .creatorId(u.id.get).description(description).save
        S.redirectTo(sourceRepo.pullRequestsUrl)
      }
      case _ => S.error("User not authentificated")
    }

  }

  def renderAvailablePullRequests = {
    urp.repo match {
      case Full(repo) => ".pull_request" #> repo.pullRequests.map(pr => {
        <div>
        <p>{a(pr.srcRepoId.obj.get.sourceTreeUrl(pr.srcRef.get), Text(pr.srcRepoId.obj.get.owner.login.get + "/" + pr.srcRepoId.obj.get.name.get + "@" + pr.srcRef))} &rarr;
        {a(pr.destRepoId.obj.get.sourceTreeUrl(pr.srcRef.get), Text(pr.destRepoId.obj.get.owner.login.get + "/" + pr.destRepoId.obj.get.name.get + "@" + pr.destRef))} created by
          {a(pr.creatorId.obj.get.homePageUrl, Text(pr.creatorId.obj.get.login.get))} at {SnippetHelper.dateFormatter.format(pr.creationDate.get)}</p>
        <p>{a(pr.homePageUrl, Text(if(!pr.description.get.isEmpty) pr.description.get else "No description"))}</p>
        </div>
      })
      case _ => "*" #> NodeSeq.Empty
    }
  }

}