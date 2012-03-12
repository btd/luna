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
import code.lib._

/**
 * User: denis.bardadym
 * Date: 9/27/11
 * Time: 3:28 PM
 */

class AdminRepoOps(urp: WithRepo) extends Loggable with SshKeyUI with RepositoryUI {
  private var collaborator_login = ""

  def renderCollaboratorsTable = w(urp.repo) { repo =>
    ".collaborator" #> repo.collaborators.map(c => {
      ".collaborator [id]" #> c.id.get.toString &
      ".collaborator_name *" #> c.login.get &
      ".collaborator_delete *" #> SHtml.a(EntityRef("times")) {
                                  (CollaboratorDoc where (_.userId eqs c.id.get) and (_.repoId eqs repo.id.get)).findAndDeleteOne
                JqId(c.id.get.toString) ~> JqRemove()}

      })
  }

  def renderSshKeysTable = w(urp.repo) {repo => keysTable(repo.keys)}

  def addCollaborator = {
    "name=login" #>
      SHtml.text(collaborator_login, {
        value: String =>
          collaborator_login = value.trim
          if (collaborator_login.isEmpty) S.error("collaborators", "Login field is empty")
      }, "placeholder" -> "login", "class" -> "textfield large") &
      "button" #> SHtml.button("Add collaborator", addNewCollaborator, "class" -> "button", "id" -> "add_collaborator_button")
  }


  def renderAddKeyForm = w(urp.repo) {repo => {
    val newKey = SshKeyRepoDoc.createRecord.ownerId(repo.id.get)
    sshKeyForm(newKey, "Add", saveSshKey(newKey))
  }}

  

  private def addNewCollaborator() = {
    urp.repo match {
      case Full(r) => {
        UserDoc.find("login", collaborator_login) match {
          case Full(u) => CollaboratorDoc.createRecord.userId(u.id.get).repoId(r.id.get).save
          case _ => S.error("collaborators", "Invaid collaborator name")
        }

      }
      case _ => S.error("Invaid repo name")
    }
  }


  def renderUpdateRepoForm = w(urp.repo) {repo => {
    repositoryForm(repo, "Update", updateRepo(repo, r => S.redirectTo(Sitemap.repoAdmin.calcHref(RepoPage(repo))))) }}

  

  def renderDeleteRepo = w(urp.repo) {repo => 
      "button" #> SHtml.button("Delete", deleteRepo(repo, r => S.redirectTo(Sitemap.userRepos.calcHref(UserPage(repo.owner)))), "class" -> "button")
  }

  def renderBranchSelector = w(urp.repo) {repo => 
      ".current_branch" #> SHtml.ajaxSelectObj(repo.git.refsHeads.zip(repo.git.branches), Empty, //TODO if inited than set
            (value : org.eclipse.jgit.lib.Ref) => {
              logger.debug("try to set current branch to " + value.getName)
              val res = repo.git.setCurrentBranch(value)//TODO successfull ?
              logger.debug(res)

               JsCmds.Noop })
  }
}