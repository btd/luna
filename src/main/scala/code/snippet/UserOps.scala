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
import entity.{DAO, Repository, User}

/**
 * User: denis.bardadym
 * Date: 9/19/11
 * Time: 2:16 PM
 */

class UserOps(up: UserPage) extends Loggable {

  private var newRepositoryName = ""

  def pageOwner_?(user: Box[User]): Boolean = user match {
    case Full(u) if u.login == up.login => true
    case _ => false
  }

  /*
  TODO при нажатии на кнопку создания вызывать диалог с формочкой
   */

  def userPage = {
    ".sub_menu *" #> (if (pageOwner_?(User.currentUser))
      SHtml.text(newRepositoryName, {
        value: String =>
          newRepositoryName = value.trim //TODO добавить проверку, что только валидные символы
          if (newRepositoryName.isEmpty) S.error("Email field are empty")
      },
      "placeholder" -> "Repo name", "class" -> "textfield large") ++ <br/> ++
        SHtml.button("New repository", createRepository, "class" -> "button", "id" -> "create_repo_button")
    else Text("User " + up.login)) &
      ".repo_list *" #> (up.user match {
        case Some(user) => {
          Repository.of(user).flatMap(repo =>
            <div class="repo_block">
              <h3>
                {repo.name}
              </h3>
              <div class="url-box">
                <ul class="clone-urls">
                  {if (repo.canPush_?(User.currentUser)) <li class="private_clone_url">
                  <a href={repo.privateSshUrl}>Ssh</a>
                </li>}<li class="public_clone_url selected">
                  <a href={repo.publicGitUrl}>Git</a>
                </li>
                </ul>
                  <input type="text" class="textfield" readonly=" " value={repo.publicGitUrl}/>
              </div>{if (pageOwner_?(User.currentUser)) <a href={"/admin/" + up.login + "/" + repo.name} class="admin_button">
                <span class="ui-icon ui-icon-gear "/>
            </a>}
            </div>
          ) ++
          (if (pageOwner_?(User.currentUser))
            Repository.collaborated(user).flatMap(repo =>
              <div class="repo_block">
                <h3>
                  {repo.name}
                  (collaborator)</h3>
                <div class="url-box">
                  <ul class="clone-urls">
                    <li class="private_clone_url">
                      <a href={repo.privateSshUrl(user)}>Ssh</a>
                    </li>
                    <li class="public_clone_url selected">
                      <a href={repo.publicGitUrl}>Git</a>
                    </li>
                  </ul>
                    <input type="text" class="textfield" readonly=" " value={repo.publicGitUrl}/>
                </div>
                <a href={"/admin/" + up.login + "/" + repo.name} class="admin_button">
                    <span class="ui-icon ui-icon-gear "/>
                </a>
              </div>
            )
          else Nil)
        }
        case _ => Text("Invalid user")
      })
  }

  private def createRepository() = {
    logger.debug("try to add new repository")

    //TODO сделаю сейчас по тупому, потом заменю на CometActor
    DAO.atomic {
      t =>
        t +: new Repository(newRepositoryName, true, up.login)
    }

  }


}