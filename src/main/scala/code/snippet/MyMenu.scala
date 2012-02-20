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
import xml.{Text, NodeSeq}
import code.model.UserDoc

/**
 * Created by IntelliJ IDEA.
 * User: den
 * Date: 24.09.11
 * Time: 15:40
 * To change this template use File | Settings | File Templates.
 */

class MyMenu {
  def your = {
    "li *" #> (UserDoc.currentUser match {
      case Full(u) => <a href={"/" + u.login.get}>Your page</a>
      case _ => Text("")
    })
  }

  def admin = {
    "li *" #> (UserDoc.currentUser match {
      case Full(u) => <a href={"/admin/" + u.login.get}>Admin</a>
      case _ => Text("")
    })
  }

  def signIn = {
    UserDoc.currentUser match {
      case Full(u) => "li *" #> SHtml.a(()=> { UserDoc.logoutCurrentUser; S.redirectTo(S.referer openOr "") }, Text("Log Out"))
      case _ => "li" #> (<li><a href="/user/m/login">Log In</a></li> ++ <li><a href="/user/m/new">Register</a></li>)
    }

  }
}