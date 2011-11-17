/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
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
import code.model._

/**
 * User: denis.bardadym
 * Date: 10/5/11
 * Time: 2:14 PM
 */

class RepoOps(urp: WithRepo) {

  def renderSourceTreeDefaultLink = w(urp.repo){repo => {
    ".repo_menu_link *" #> (S.attr("current") match {
      case Full(_) => Text("Sources")
      case _ => a(repo.sourceTreeUrl, Text("Sources"))
    })
  }} 

  def renderCommitsDefaultLink = w(urp.repo){repo => {
    ".repo_menu_link *" #> (S.attr("current") match {
      case Full(_) => Text("Commits")
      case _ => a(repo.commitsUrl, Text("Commits"))
    })
  }}  

  def renderPullRequestsLink = w(urp.repo){repo => {
    ".repo_menu_link *" #> (S.attr("current") match {
      case Full(_) => Text("Pull requests") 
      case _ => a(repo.pullRequestsUrl, Text("Pull requests"))
    })
  }} 

  def renderRepositoryBlock = w(urp.user){u =>  w(urp.repo){repo => 
      ".repo *" #> (UserDoc.currentUser match {
        case Full(cu) if (cu.login.get == u.login.get) => {
            ".repo_name *" #> a(repo.sourceTreeUrl, Text(repo.name.get)) &
            ".clone-url *" #> (repo.cloneUrlsForCurrentUser.map(url => "a" #> a(url._1, Text(url._2)))) &
            ".admin_page *" #> a("/admin" + repo.homePageUrl, Text("admin")) & 
            ".fork *" #> a("", Text("fork it")) &
            ".toggle_open" #> NodeSeq.Empty
        }
        case Full(cu) => {        
            ".repo_name *" #> a(repo.sourceTreeUrl, Text(repo.name.get)) &
            ".clone-url *" #> (repo.cloneUrlsForCurrentUser.map(url => a(url._1, Text(url._2)))) &
            ".admin_page" #> NodeSeq.Empty & 
            ".fork *" #> a("", Text("fork it")) &
            ".toggle_open" #> NodeSeq.Empty
          }
        case _ => {
            ".repo_name *" #> a(repo.sourceTreeUrl, Text(repo.name.get)) &
            ".clone-url *" #> (repo.cloneUrlsForCurrentUser.map(url => a(url._1, Text(url._2)))) &
            ".admin" #> NodeSeq.Empty 
        }
    })
  }} 

  
}