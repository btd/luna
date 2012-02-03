/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package code.snippet

import bootstrap.liftweb._
import net.liftweb._
import util.Helpers._
import http._
import common._
import code.model._
import SnippetHelper._
import xml._

import com.foursquare.rogue.Rogue._

/**
 * User: denis.bardadym
 * Date: 9/19/11
 * Time: 2:16 PM
 */

class UserOps(up: WithUser) extends Loggable with RepositoryUI {

  def renderNewRepositoryForm = w(up.user) {u => w(UserDoc.currentUser){cu => {
    val repo = RepositoryDoc.createRecord.ownerId(u.id.get)
    if(u.login.get == cu.login.get) repositoryForm(repo, "Add", saveRepo(repo))
    else cleanAll
  }}}

  def renderRepositoryList = w(up.user) {u => 
    if(u.repos.isEmpty)
    ".repo" #> NodeSeq.Empty
    else 
    ".repo" #> ((u.publicRepos ++ (if(u.is(UserDoc.currentUser)) u.privateRepos else Nil)).map(repo => {
      ".repo [class+]" #> (if(repo.open_?.get) "public" else "private" ) &
        ".repo_name *" #> a(repo.sourceTreeUrl, Text(repo.name.get)) &
        ".clone-url" #> (repo.cloneUrlsForCurrentUser.map(url => "a" #> a(url._1, Text(url._2)))) &
        (UserDoc.currentUser match {
          case Full(cu) if (cu.login.get == u.login.get) => {
              ".admin_page *" #> a("/admin" + repo.homePageUrl, Text("admin")) & 
              ".fork *" #> SHtml.a(makeFork(repo, cu) _, Text("fork it")) & //later will be added js append
              ".toggle_open *" #> SHtml.a(toggleOpen(repo) _, Text(if (repo.open_?.get) "make private" else "make public"))
          }
          case Full(cu) => {        
              ".admin_page" #> NodeSeq.Empty & 
              ".fork *" #> SHtml.a(makeFork(repo, cu) _, Text("fork it")) &
              ".toggle_open" #> NodeSeq.Empty
            }
          case _ => {
              ".admin" #> NodeSeq.Empty 
        }
      }) 
    }) ++
    (if(u.is(UserDoc.currentUser)) u.collaboratedRepos else Nil).map(repo => {
      ".repo [class+]" #> "collaborated" &
      ".admin_page" #> NodeSeq.Empty & 
      ".fork *" #> SHtml.a(makeFork(repo, UserDoc.currentUser.get) _, Text("fork it")) &
      ".toggle_open" #> NodeSeq.Empty
        
    }) 
    )  
  }
}