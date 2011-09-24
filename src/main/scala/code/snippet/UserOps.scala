/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package code.snippet

import bootstrap.liftweb.UserPage
import xml.Text
import net.liftweb._
import util.Helpers._
import http._
import common._
import entity.{SshKey, DAO, Repository, User}

/**
 * User: denis.bardadym
 * Date: 9/19/11
 * Time: 2:16 PM
 */

class UserOps(up: UserPage) extends Loggable {



  def pageOwner_?(user: Box[User]) : Boolean = user match {
    case Full(u) if u.login == up.login => true
    case _ => false
  }

  def userPage = {
    ".sub_menu" #> (if (pageOwner_?(User.currentUser))
      SHtml.button("New repository", createRepository, "class"->"button")
    else Text("User " + up.login)) &
      ".repo_list" #> (Repository.ownedBy(up.login).flatMap(repo =>
        <div class="repo_block">
          <h3>{repo.name}</h3>
          <div class="url-box">
            <ul class="clone-urls">
              {if (repo.canPush_?(User.currentUser)) <li class="private_clone_url"><a href={repo.privateSshUrl}>Ssh</a></li>}
              <li class="public_clone_url selected"><a href={repo.publicGitUrl}>Git</a></li>
            </ul>
            <input type="text" class="textfield" readonly="" value={repo.publicGitUrl}/>
          </div>
        </div>
      ))
  }
  //TODO сделать так чтобы она заработала
  private def createRepository() = {

  }


}

/*TODO
Добавить нормальные формы авторизации

*/