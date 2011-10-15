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
import js.jquery.JqJE._
import js.jquery.JqJsCmds._
import util.PassThru
import SnippetHelper._
import xml.{Node, NodeSeq, Text}

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
          case _ => "*" #> NodeSeq.Empty
        }
      }
      case _ => PassThru
    }
  }



  def renderAvailableRepositoryList = {
    up.user match {
      case Full(u) => {
        ".repo_block" #> (UserDoc.currentUser match {
          case Full(cu) if (u.login.get == cu.login.get) => {
            u.repos.map(repo => urlBox(Full(repo), repoName _, cloneButtonAppend)) ++
              CollaboratorDoc.findAll("userId", u.id.get).map(cDoc => urlBox(cDoc.repoId.obj, r => a(r.sourceTreeUrl, Text(r.name.get + " (collaborator)")), cloneButtonAppend))

          }

          case _ => {
            u.repos.map(repo =>
              urlBox(Full(repo), repoName _, cloneButtonAppend)
            )
          }
        })

      }
      case _ => PassThru
    }
  }


  private def createRepository() = {
    logger.debug("try to add new repository")


    RepositoryDoc.createRecord.name(newRepositoryName).ownerId(up.user.get.id.is).save


  }


}