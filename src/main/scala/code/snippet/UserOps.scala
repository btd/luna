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
import util.Helpers._
import http._
import common._
import code.model._
import code.lib._
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
      val rp = RepoPage(repo)
      ".repo [class+]" #> (if(repo.open_?.get) "public" else "private" ) &
      ".repo_name *" #> a(Sitemap.defaultTree.calcHref(rp), Text(repo.name.get)) &
      ".clone-url" #> (repo.cloneUrlsForCurrentUser.map(url => "a" #> a(url._1, Text(url._2)))) &
        (UserDoc.currentUser match {
          case Full(cu) if (cu.login.get == u.login.get) => {
              ".admin_page *" #> a(Sitemap.repoAdmin.calcHref(rp), Text("admin")) & 
              ".fork *" #> SHtml.a(makeFork(repo, cu) _, Text("fork it")) & //later will be added js append
              ".notification_page *" #> a(Sitemap.notification.calcHref(RepoPage(repo)), Text("notify")) &
              ".toggle_open *" #> SHtml.a(toggleOpen(repo) _, Text(if (repo.open_?.get) "make private" else "make public")) &
              (repo.forkOf.obj.map(fr => ".origin_link *" #> a(Sitemap.defaultTree.calcHref(RepoPage(fr)), Text("origin"))) openOr 
                  ".origin_link" #> NodeSeq.Empty)

          }
          case Full(cu) => {        
              ".admin_page" #> NodeSeq.Empty & 
              ".fork *" #> SHtml.a(makeFork(repo, cu) _, Text("fork it")) &
              ".toggle_open" #> NodeSeq.Empty &
              ".notification_page *" #> a(Sitemap.notification.calcHref(RepoPage(repo)), Text("notify")) &
              (repo.forkOf.obj.map(fr => ".origin_link *" #> a(Sitemap.defaultTree.calcHref(RepoPage(fr)), Text("origin"))) openOr 
                  ".origin_link" #> NodeSeq.Empty)
            }
          case _ => {
              (repo.forkOf.obj match {
                  case Full(fr) => {
                    ".origin_link *" #> a(Sitemap.defaultTree.calcHref(RepoPage(fr)), Text("origin")) &
                    ".admin_page" #> NodeSeq.Empty & 
                    ".toggle_open" #> NodeSeq.Empty &
                    ".notification_page" #> NodeSeq.Empty &
                    ".fork" #> NodeSeq.Empty
                  }
                  case _ =>  ".admin" #> NodeSeq.Empty 
                })
             
        }
      }) 
    }) ++
    (if(u.is(UserDoc.currentUser)) u.collaboratedRepos else Nil).map(repo => {
      ".repo [class+]" #> "collaborated" &
      ".repo_name *" #> a(Sitemap.defaultTree.calcHref(RepoPage(repo)), Text(repo.name.get)) &
      ".clone-url" #> (repo.cloneUrlsForCurrentUser.map(url => "a" #> a(url._1, Text(url._2)))) &
      ".admin_page" #> NodeSeq.Empty & 
      ".fork *" #> SHtml.a(makeFork(repo, UserDoc.currentUser.get) _, Text("fork it")) &
      ".toggle_open" #> NodeSeq.Empty &
      (repo.forkOf.obj.map(fr => ".origin_link *" #> a(Sitemap.defaultTree.calcHref(RepoPage(fr)), Text("origin"))) openOr 
                  ".origin_link" #> NodeSeq.Empty)       
    }) 
    )  
  }
}