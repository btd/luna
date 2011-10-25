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
import xml.Text
import code.model.{UserDoc, PullRequestDoc, RepositoryDoc}

/**
 * User: denis.bardadym
 * Date: 10/24/11
 * Time: 2:43 PM
 */

class PullRequestOps(urp: RepoPage) extends Loggable {

  private var sourceRepo: RepositoryDoc = null

  private var sourceRef = ""

  private var destRepo: RepositoryDoc = null

  private var destRef = ""

  def renderUserClones = {
    urp.repo match {
      case Full(repo) => {
        ".repo_selector" #> SHtml.selectObj(urp.user.get.repos.filter(r => r.forkOf.get == repo.forkOf.get).map(r => r -> r.name.get),
                Full(repo), (r: RepositoryDoc) => { sourceRepo = r } , "class" -> "selectmenu repo_selector") &
        "name=srcRef" #> SHtml.text(sourceRef,  s =>
            { sourceRef = s.trim
              if(sourceRef.isEmpty) S.error("Source reference is empty")
            }, "class" -> "textfield")
      }
      case _ => PassThru
    }
  }

  def renderAllClones = {
    urp.repo match {
      case Full(repo) => {
         ".repo_selector" #> SHtml.selectObj[RepositoryDoc]( (repo.forkOf.obj.get -> (repo.forkOf.obj.get.owner.login + "/" + repo.forkOf.obj.get.name.get)) :: RepositoryDoc.allClonesExceptOwner(repo).map(r => r -> (r.owner.login + "/" + r.name.get)),
                repo.forkOf.obj, (r: RepositoryDoc) => { destRepo = r } , "class" -> "selectmenu repo_selector") &
        "name=destRef" #> SHtml.text(destRef,  s =>
            { destRef = s.trim
              if(destRef.isEmpty) S.error("Destination reference is empty")
            }, "class" -> "textfield")
      }
      case _ => PassThru
    }
  }

  def renderForm = {
     urp.repo match {
      case Full(repo) => {
        "button" #> SHtml.button(Text("new pull request"), processNewPullRequest , "class" -> "button", "id" -> "create_new_pull_request_button")
      }
      case _ => PassThru
    }
  }

  def processNewPullRequest() = {
    UserDoc.currentUser match  {
      case Full(u) => {
        PullRequestDoc.srcRepoId(sourceRepo.id.get).destRepoId(destRepo.id.get).srcRef(sourceRef).destRef(destRef).creatorId(u.id.get).save
        S.redirectTo(sourceRepo.pullRequestUrl)
      }
      case _ => S.error("User not authentificated")
    }

  }
}