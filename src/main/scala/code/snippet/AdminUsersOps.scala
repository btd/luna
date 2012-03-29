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

import bootstrap.liftweb._
import net.liftweb._
import http._
import js.{JsCmds, JsCmd}
import js.jquery._
import JqJE._
import JqJsCmds._
import util.Helpers._
import common._

import com.foursquare.rogue.Rogue._
import xml.{NodeSeq, Text, EntityRef}
import code.model._
import code.lib.Sitemap._
import org.bson.types.ObjectId

/**
 * User: denis.bardadym
 * Date: 9/27/11
 * Time: 3:28 PM
 */

class AdminUsersOps extends Loggable with UserUI {

	def changeAdminOption(u: UserDoc): JsCmd = {
		u.admin(!u.admin.get).save
		Jq("#" + u.id.get.toString + " " + ".user_admin") ~> 
			JqHtml(SHtml.a(Text(u.admin.get.toString)) {
				changeAdminOption(u)
			})
	} 

	def renderUsersTable = {
		".user" #> UserDoc.allButNot(UserDoc.currentUser.get.id.get).map(u => 
	      ".user [id]" #> u.id.get.toString &
	      ".user *" #> (
		      ".user_name *" #> u.login.get &
		      ".user_admin *" #> SHtml.a(Text(u.admin.get.toString)) {
		      	changeAdminOption(u)
		      } &
		      ".user_delete *" #> SHtml.a(EntityRef("times")) {
		                            (UserDoc.where(_.id eqs u.id.get).findAndDeleteOne)
		                            JqId(u.id.get.toString) ~> JqRemove() 
		                          }

		      )	
	      	)
	      
	}

	def addNewUser = {
		val user = UserDoc.createRecord
		userForm(user, "Add", saveUser(user, u => { S.redirectTo(adminUsers.loc.calcDefaultHref) }))
	}
}
//
