/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package code.snippet

import bootstrap.liftweb._
import net.liftweb._
import http._
import js.jquery._
import JqJE._
import JqJsCmds._
import util.Helpers._
import common._
import util.PassThru
import com.foursquare.rogue.Rogue._
import xml.{NodeSeq, Text}
import code.model._
import org.bson.types.ObjectId
import SnippetHelper._

/**
 * User: denis.bardadym
 * Date: 9/27/11
 * Time: 3:28 PM
 */

class AdminRepoOps(urp: WithRepo) extends Loggable with SshKeyUI with RepositoryUI {
  private var collaborator_login = ""

  def renderCollaboratorsTable = w(urp.repo) { repo =>
    ".collaborators" #> repo.collaborators.map(c => {
      ".collaborator [id]" #> c.id.get.toString &
      ".collaborator *" #> (".collaborator_name *" #> c.login.get &
            ".collaborator_delete *" #> SHtml.a(Text("X")) {
                                  (CollaboratorDoc where (_.userId eqs c.id.get) and (_.repoId eqs repo.id.get)).findAndDeleteOne
                JqId(c.id.get.toString) ~> JqRemove()})

          })
  }

  def renderSshKeysTable = w(urp.repo) {repo =>
    keysTable(repo.keys)
  }

  def addCollaborator = {
    "name=login" #>
      SHtml.text(collaborator_login, {
        value: String =>
          collaborator_login = value.trim
          if (collaborator_login.isEmpty) S.error("collaborators", "Login field is empty")
      }, "placeholder" -> "login", "class" -> "textfield large") &
      "button" #> SHtml.button("Add collaborator", addNewCollaborator, "class" -> "button", "id" -> "add_collaborator_button")
  }


  def renderAddKeyForm = w(urp.repo) {repo => sshKeyForm(addNewKey(repo))}

  private def addNewKey(repo: RepositoryDoc)() = {
        if (!ssh_key.isEmpty) {
          SshKeyDoc.createRecord.ownerId(repo.ownerId.is).rawValue(ssh_key).ownerRepoId(repo.id.is).saveTheRecord
        }
  }

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


  def renderUpdateRepoForm = w(urp.repo) {repo => repositoryForm(repo.name.get, updateRepo(repo)) }   

  private def updateRepo(repo: RepositoryDoc)() = {
        if (!name.isEmpty) {
          if (repo.owner.repos.contains((r: RepositoryDoc) => r.name.get == name)) {
            S.error("repo", "Invalid repo")
          } else {
            if (!name.matches("""[a-zA-Z0-9\.\-]+""")) {
              S.error("repo", "Repo name can contains only ASCII letters, digits, .(point), -")
            }
            else {
              repo.name(name).saveTheRecord
              S.redirectTo("/admin" + repo.homePageUrl)
            }

          }

        }
  }

  def renderDeleteRepo = w(urp.repo) {repo => 
      "button" #> SHtml.button("Delete", processDelete(repo), "class" -> "button")
  }

  def processDelete(repo: RepositoryDoc)() = { 
      val redirect = repo.owner.homePageUrl
      repo.deleteDependend

      repo.delete_!

      S.redirectTo(redirect)
   }
}