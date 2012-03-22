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
import js.JsCmds
import js.jquery._
import JqJE._
import JqJsCmds._
import util.Helpers._
import common._
import util.PassThru
import com.foursquare.rogue.Rogue._
import xml.{NodeSeq, Text, EntityRef}
import code.model._
import org.bson.types.ObjectId
import SnippetHelper._
import code.lib.Sitemap._

/**
 * User: denis.bardadym
 * Date: 9/27/11
 * Time: 3:28 PM
 */

class AdminRepoOps(repo: RepositoryDoc) extends Loggable with SshKeyUI with RepositoryUI {
  private var collaborator_login = ""

  def renderCollaboratorsTable = 
    ".collaborator" #> repo.collaborators.map(c => {
      ".collaborator [id]" #> c.id.get.toString &
      ".collaborator_name *" #> c.login.get &
      ".collaborator_delete *" #> SHtml.a(EntityRef("times")) {
                                  (CollaboratorDoc where (_.userId eqs c.id.get) and (_.repoId eqs repo.id.get)).findAndDeleteOne
                                  JqId(c.id.get.toString) ~> JqRemove() 
                                }

      })


  def renderSshKeysTable = keysTable(repo.keys)

  def addCollaborator = 
    "name=login" #>
      SHtml.text(collaborator_login, {
        value: String =>
          collaborator_login = value.trim
          if (collaborator_login.isEmpty) S.error("collaborators", "Login field is empty")
      }, "placeholder" -> "login", "class" -> "textfield large") &
      "button" #> SHtml.button("Add collaborator", addNewCollaborator, "class" -> "button", "id" -> "add_collaborator_button")



  def renderAddKeyForm = {
    val newKey = SshKeyRepoDoc.createRecord.ownerId(repo.id.get)
    sshKeyForm(newKey, "Add", saveSshKey(newKey))
  }

  

  private def addNewCollaborator() = {

        UserDoc.byName(collaborator_login) match {
          case Some(u) => CollaboratorDoc.createRecord.userId(u.id.get).repoId(repo.id.get).save
          case _ => S.error("collaborators", "Invaid collaborator name")
        }

      
  }


  def renderUpdateRepoForm = 
    repositoryForm(repo, "Update", updateRepo(repo, r => S.redirectTo(repoAdmin.calcHref(r))))

  

  def renderDeleteRepo = 
      "button" #> SHtml.button("Delete", deleteRepo(repo, r => 
        S.redirectTo(userRepos.calcHref(repo.owner))), "class" -> "button")


  def renderBranchSelector = 
      ".current_branch" #> SHtml.ajaxSelectObj(
            repo.git.refsHeads.zip(repo.git.branches), 
            Empty, //TODO if inited than set
            (value : org.eclipse.jgit.lib.Ref) => {

              val res = repo.git.setCurrentBranch(value)//TODO successfull ?
              JsCmds.Noop 
            })
  
}