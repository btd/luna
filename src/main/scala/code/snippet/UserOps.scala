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

import js.jquery._
import js.jquery.JqJE._
import js.jquery.JqJsCmds._

import code.model._
import code.lib._
import code.lib.Sitemap._
import SnippetHelper._
import xml._

import com.foursquare.rogue.Rogue._

/**
 * User: denis.bardadym
 * Date: 9/19/11
 * Time: 2:16 PM
 */

class UserOps(user: UserDoc) extends Loggable with RepositoryUI {

  def renderNewRepositoryForm: NodeSeq => NodeSeq = 
    (for { 
           currentUser <- UserDoc.currentUser
           repo = RepositoryDoc.createRecord.ownerId(user.id.get)
           if(user.login.get == currentUser.login.get)
     } yield {   
           repositoryForm(repo, "Add", saveRepo(repo)) 
     }) openOr cleanAll
 

  def renderRepositoryList = {
    val repos = user.publicRepos ++ 
      (if(user.is(UserDoc.currentUser)) user.privateRepos ++ user.collaboratedRepos else Nil)

    

    if(repos.isEmpty)
      "*" #> <span class="large">There is no repo</span>
    else 
     ".repo" #> repos.map(repo =>
       renderRepositoryBlock(repo, user, 
        r => a(defaultTree.calcHref(repo), 
          if(repo.ownerId.get == user.id.get) Text(repo.name.get)
          else Text(repo.owner.login.get + "/" + repo.name.get))))
    
    
  }
}