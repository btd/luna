/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package code.snippet

import bootstrap.liftweb.UserPage
import net.liftweb.util.Helpers._
import entity.User
import xml.Text

/**
 * User: denis.bardadym
 * Date: 9/19/11
 * Time: 2:16 PM
 */

class UserOps(up: UserPage) {

  def render = {
    ".sub_menu" #> (if (User.loggedIn_? && (up.login == User.currentUserId.open_!))
      <button class="button" type="button" value="New">New repository</button> else  Text("") ) &
    ".repo_list" #> ()
  }
}