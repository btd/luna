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
import code.model.RepositoryDoc
import xml.Text


/**
 * User: denis.bardadym
 * Date: 10/24/11
 * Time: 2:43 PM
 */

class PullRequestOps(urp: RepoPage) extends Loggable {
  def renderUserClones = {
    urp.repo match {
      case Full(repo) => {
        ".repo_selector" #> (
          <select class="selectmenu repo_selector">
            <option value={repo.name.get} selected="selected">{repo.name.get}</option> ++
            {urp.user.get.repos.filter(r => r.forkOf.get == repo.forkOf.get && r.id.get != repo.id.get).map(r =>
            <option value={r.name.get}>{r.name.get}</option>
          )}
          </select>)
      }
      case _ => PassThru
    }
  }

  def renderAllClones = {
    urp.repo match {
      case Full(repo) => {
        ".repo_selector" #> (
          <select class="selectmenu repo_selector">
            <option value={repo.forkOf.obj.get.name.get} selected="selected">{repo.forkOf.obj.get.owner.login.get + "/" + repo.forkOf.obj.get.name.get}</option> ++
            {RepositoryDoc.allClonesExceptOwner(repo).map(r =>
            <option value={r.name.get}>{r.owner.login.get + "/" + r.name.get}</option>
          )}
          </select>)
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

  def processNewPullRequest() = {}
}