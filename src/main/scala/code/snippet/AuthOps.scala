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

import net.liftweb._
import common.{Full, Loggable}
import util.Helpers._
import http._
import code.model._
import code.lib.Sitemap._
import SnippetHelper._


class AuthOps extends Loggable with UserUI with SshKeyUI {
  private var email = ""
  private var password = ""
  private var login = ""

  def renderNewUserForm = {
    val newUser = UserDoc.createRecord
    val newKey = SshKeyUserDoc.createRecord.ownerId(newUser.id.get)

    userForm(newUser) &
    sshKeyForm(newKey) &
    button("Register", () => {
      fillUser(newUser)
      fillKey(newKey)
      newUser.validate match {
        case Nil => newKey.validate match {
            case Nil => {
              newUser.save
              newKey.save
              //logger.debug(newUser)
              UserDoc.logUserIn(newUser, ()=> S.redirectTo(userRepos.calcHref(newUser)))
            }
            case l => l.foreach(fe => S.error(fe.msg))
          }
        case l => l.foreach(fe => S.error(fe.msg))
      }
    })
  }
 

  
}