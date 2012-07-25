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
package code.rest

import net.liftweb._
import http._
import common._
import util._
import json._
import json.JsonDSL._
import Helpers._
import code.model._
import xml._
import Utility._
import com.foursquare.rogue.Rogue._
import net.liftweb.http.rest._

object ApiV1 extends Loggable with RestHelper {

  def loginSuccessResponce(user: UserDoc) = {
    JsonResponse(JObject(JField("access_token", UserDoc.logUserIn(user)) :: JField("user", user.asJValue) :: Nil))
  }

  serve { "api" / "1" prefix {
    case "auth" :: "token" :: Nil JsonGet _ => {
      (S.param("login"), S.param("password")) match {
        case (Full(login), Full(password)) => 
          UserDoc.byName(login) match {
            case Some(user) if user.password.match_?(password) => loginSuccessResponce(user)
            case Some(user) => ForbiddenResponse("Wrong password.")
            case _ => 
              val user = UserDoc.createRecord.login(login).password(password)
              user.validate match {
                case Nil => 
                  user.save 
                  loginSuccessResponce(user)
                case l => ForbiddenResponse(l.map(_.msg).mkString(". "))
              }
            
          }
        case _ => BadResponse()
      }
    }
    case "wiki" :: "root" :: Nil JsonGet _ => JsonResponse(JObject(JField("content", code.snippet.WelcomeWiki.finalContent) :: Nil))
  }}


}