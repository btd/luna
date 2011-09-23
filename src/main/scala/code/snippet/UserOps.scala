/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package code.snippet

import bootstrap.liftweb.UserPage
import net.liftweb.util.Helpers._
import entity.{Repository, User}
import xml.{Attribute, Text}
import net.liftweb.http.{SHtml, S}

/**
 * User: denis.bardadym
 * Date: 9/19/11
 * Time: 2:16 PM
 */

class UserOps(up: UserPage) {

  def render = {  //TODO    currentUserId может быть пустой
    ".sub_menu" #> (if (User.loggedIn_? && (up.login == User.currentUserId.open_!))
      <button class="button" type="button" value="New">New repository</button>
    else Text("")) &
      ".repo_list" #> (Repository.ownedBy(up.login).flatMap(repo =>
        <div class="repo_block">
          <h3>
            {repo.name}
          </h3>
          <div class="url-box">
            <ul class="clone-urls">
              {if(up.login == User.currentUserId.open_!)  //TODO будет пофикшено когда будет доступ множественный
              //TODO переместить генерацию в соответствующий оьбъект
              <li class="private_clone_url">
                {<a>SSH</a> % Attribute("href", Text(up.login + "@" + S.hostName + ":" + repo.name), null)}
              </li>
              }
              <li class="public_clone_url selected">
                {<a>Git</a> % Attribute("href", Text("git://" + S.hostName + "/" + up.login + "/" + repo.name), null)}
              </li>
            </ul>
              {<input type="text" class="textfield" readonly=""/> % Attribute("value", Text("git://" + S.hostName + "/" + up.login + "/" + repo.name), null)}

          </div>


        </div>
      ))
  }
}