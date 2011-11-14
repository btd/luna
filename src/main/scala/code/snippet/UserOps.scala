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
import code.model.{CollaboratorDoc, UserDoc, RepositoryDoc}
import SnippetHelper._
import xml.{NodeSeq, Text}

import com.foursquare.rogue.Rogue._

/**
 * User: denis.bardadym
 * Date: 9/19/11
 * Time: 2:16 PM
 */

class UserOps(up: WithUser) extends Loggable with RepositoryUI {

  private var newRepositoryName = ""

  def renderUserName = {
    "*" #> up.user.get.login.get
  }

  def renderNewRepositoryForm = w(up.user) {u => w(UserDoc.currentUser){cu => {
    if(u.login.get == cu.login.get) repositoryForm("", createRepository)
    else cleanAll
  }}}
 


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
              urlBox(Full(repo), repoName _, cloneButtonRedirect)
            )
          }
        })

      }
      case _ => "*" #> NodeSeq.Empty
    }
  }


  private def createRepository() = {
    logger.debug("try to add new repository")

    if (!newRepositoryName.matches("""[a-zA-Z0-9\.\-]+""")) S.error("Repository name can contains only ASCII letters, digits, .(point), -")
    else {
      RepositoryDoc where (_.ownerId eqs up.user.get.id.get) and (_.name eqs newRepositoryName) get match {
        case Some(_) => S.error("You already have repository with such name")
        case None => RepositoryDoc.createRecord.name(newRepositoryName).ownerId(up.user.get.id.is).save
      }

    }


  }


}