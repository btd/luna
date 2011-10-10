/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package code.snippet

import bootstrap.liftweb.UserPage
import net.liftweb._
import util.Helpers._
import http._
import common._
import code.model.{CollaboratorDoc, UserDoc, RepositoryDoc}
import util.PassThru
import SnippetHelper._
import xml.Text

/**
 * User: denis.bardadym
 * Date: 9/19/11
 * Time: 2:16 PM
 */

class UserOps(up: UserPage) extends Loggable {

  private var newRepositoryName = ""

  def renderNewRepositoryForm = {
    up.user match {
      case Full(u) => {
        UserDoc.currentUser match {
          case Full(cu) if (u.login.get == cu.login.get) => {
            "input" #> SHtml.text(newRepositoryName, {
              value: String =>
                newRepositoryName = value.trim //TODO добавить проверку, что только валидные символы
                if (newRepositoryName.isEmpty) S.error("Email field are empty")
            },
            "placeholder" -> "Repo name", "class" -> "textfield large") &
              "button" #> SHtml.button("New repository", createRepository, "class" -> "button", "id" -> "create_repo_button")
          }
          case _ => PassThru
        }
      }
      case _ => PassThru
    }
  }

  def renderAvailableRepositoryList = {
    up.user match {
      case Full(u) => {
        ".repo_list *" #> (UserDoc.currentUser match {
          case Full(cu) if (u.login.get == cu.login.get) => {
            u.repos.map(repo => urlBox(Full(repo), r => a(r.sourceTreeUrl, Text(r.name.get)))) ++
            CollaboratorDoc.findAll("userId", u.id.get).map( cDoc => urlBox(cDoc.repoId.obj, r => a(r.sourceTreeUrl, Text(r.name.get + " (collaborator)"))))

          }

          case _ => {
            u.repos.map(repo =>
              urlBox(Full(repo), r => a(r.sourceTreeUrl, Text(r.name.get)))
            )
          }
        })

      }
      case _ => PassThru
    }
  }


  private def createRepository() = {
    logger.debug("try to add new repository")

    //TODO сделаю сейчас по тупому, потом заменю на CometActor
    RepositoryDoc.createRecord.name(newRepositoryName).ownerId(up.user.get.id.is).save


  }


}