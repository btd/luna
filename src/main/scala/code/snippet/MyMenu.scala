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

import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.util.Helpers._
import net.liftweb.util.Props
import xml.{Text, NodeSeq}
import code.model.UserDoc
import code.lib.Sitemap._
import main.Constants._

/**
 * Created by IntelliJ IDEA.
 * User: den
 * Date: 24.09.11
 * Time: 15:40
 * To change this template use File | Settings | File Templates.
 */

class MyMenu {
  def your = 
    UserDoc.currentUser match {
      case Full(u) => "li *" #>  <a href={userRepos.calcHref(u)}>Your page</a>
      case _ => "li" #> NodeSeq.Empty
    }
  

  def admin = 
    UserDoc.currentUser match {
      case Full(u) => "li *" #> <a href={userAdmin.calcHref(u)}>Admin</a>
      case _ => "li" #> NodeSeq.Empty
    }
  

  def signIn = 
    UserDoc.currentUser match {
      case Full(u) => "li *" #> SHtml.a(()=> { UserDoc.logoutCurrentUser; S.redirectTo(S.referer openOr "") }, Text("Log Out"))
      case _ => "li" #> (<li><a href={login.loc.calcDefaultHref}>Log In</a></li> ++ 
                          (if(Props.getBool(USER_REGISTRATION_ENABLED, true))
                            <li><a href={newUser.loc.calcDefaultHref}>Register</a></li>
                          else NodeSeq.Empty))
    }

  def users = {
    UserDoc.currentUser match {
      case Full(u) if u.admin.get => "li *" #> <a href={adminUsers.loc.calcDefaultHref}>Users</a>
      case _ => "li" #> NodeSeq.Empty
    }
  }
}