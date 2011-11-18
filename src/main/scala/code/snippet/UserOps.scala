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
    ".repo" #> (u.repos.map(repo => {
      ".repo [class+]" #> "public" &
        (UserDoc.currentUser match {
          case Full(cu) if (cu.login.get == u.login.get) => {
              ".repo_name *" #> a(repo.sourceTreeUrl, Text(repo.name.get)) &
              ".clone-url" #> (repo.cloneUrlsForCurrentUser.map(url => "a" #> a(url._1, Text(url._2)))) &
              ".admin_page *" #> a("/admin" + repo.homePageUrl, Text("admin")) & 
              ".fork *" #> SHtml.a(makeFork(repo, cu) _, Text("fork it")) & //later will be added js append
              ".toggle_open" #> NodeSeq.Empty
          }
          case Full(cu) => {        
              ".repo_name *" #> a(repo.sourceTreeUrl, Text(repo.name.get)) &
              ".clone-url" #> (repo.cloneUrlsForCurrentUser.map(url => "a" #> a(url._1, Text(url._2)))) &
              ".admin_page" #> NodeSeq.Empty & 
              ".fork *" #> SHtml.a(makeFork(repo, cu) _, Text("fork it")) &
              ".toggle_open" #> NodeSeq.Empty
            }
          case _ => {
          ".repo_name *" #> a(repo.sourceTreeUrl, Text(repo.name.get)) &
          ".clone-url" #> (repo.cloneUrlsForCurrentUser.map(url => "a" #> a(url._1, Text(url._2)))) &
          ".admin" #> NodeSeq.Empty 
        }
      }) 
    }) ++
    (UserDoc.currentUser match {
      case Full(cu) if (cu.login.get == u.login.get) => u.collaboratedRepos
      case _ => Nil 
    }).map(repo => {
      ".repo [class+]" #> "collaborated" &
        (     ".repo_name *" #> a(repo.sourceTreeUrl, Text(repo.name.get)) &
              ".clone-url" #> (repo.cloneUrlsForCurrentUser.map(url => "a" #> a(url._1, Text(url._2)))) &
              ".admin_page" #> NodeSeq.Empty & 
              ".fork *" #> SHtml.a(makeFork(repo, UserDoc.currentUser.get) _, Text("fork it")) &
              ".toggle_open" #> NodeSeq.Empty
        ) 
      })
    )  
  }
}