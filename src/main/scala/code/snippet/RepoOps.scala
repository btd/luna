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
import js._
import code.model._

/**
 * User: denis.bardadym
 * Date: 10/5/11
 * Time: 2:14 PM
 */

class RepoOps(urp: WithRepo) {

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
                    ".toggle_open *" #> SHtml.a(toggleOpen(repo) _, Text(if (repo.open_?.get) "make private" else "make public")) &
                    (repo.forkOf.obj.map(fr => ".origin_link *" #> a(fr.sourceTreeUrl, Text("origin"))) openOr 
                          ".origin_link" #> NodeSeq.Empty)
                }
                case Full(cu) => {     
                    ".admin_page" #> NodeSeq.Empty & 
                    ".fork *" #> SHtml.a(makeFork(repo, cu) _, Text("fork it")) &
                    ".toggle_open" #> NodeSeq.Empty &
                    (repo.forkOf.obj.map(fr => ".origin_link *" #> a(fr.sourceTreeUrl, Text("origin"))) openOr 
                          ".origin_link" #> NodeSeq.Empty)
                  }
                case _ => {
                    (repo.forkOf.obj match {
                          case Full(fr) => {
                            ".origin_link *" #> a(fr.sourceTreeUrl, Text("origin")) &
                            ".admin_page" #> NodeSeq.Empty & 
                            ".toggle_open" #> NodeSeq.Empty &
                            ".fork" #> NodeSeq.Empty
                          }
                          case _ =>  ".admin" #> NodeSeq.Empty 
                        })
                }
            }))
  }} 

  

  
}