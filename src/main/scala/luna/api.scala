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
package luna.api

import unfiltered.request._
import unfiltered.response._

import luna.session._
import luna.model._
import luna.help._

import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._



object AccessToken extends Params.Extract("access_token", Params.first ~> Params.nonempty)

object Login extends Params.Extract("login", Params.first ~> Params.nonempty)
object Password extends Params.Extract("login", Params.first ~> Params.nonempty)

object V1 extends Loggable {
  def intent = unfiltered.Cycle.Intent[Any, Any] {
    case req @ GET(Path(Seg("api" :: "1" :: "auth" :: "token" :: Nil))) => req match {
        case Params(AccessToken(accessToken)) => 
          Session.get(accessToken).user match {
            case None => Forbidden ~> ResponseString("There is no any session for this token")
            case Some(user) => Json(JObject(JField("access_token", accessToken) :: JField("user", user.asJValue) :: Nil))
          }
        case Params(Login(login) & Password(password)) => 
          def successfulAuthResponse(user: User) = {
            val token = Session.put(SessionData(Some(user)))
            Json(JObject(JField("access_token", token) :: JField("user", user.asJValue) :: Nil))
          }

          logger.debug("Try to find user by login: " + login)

          Users.byLogin(login) match {
            case Some(user) if user.password.match_?(password) => successfulAuthResponse(user)
            case Some(user) => Forbidden ~> ResponseString("Wrong password")
            case _ =>
              logger.debug("User not founded")
              val user = User(login = login, password = PasswordHash(password))
              logger.debug("User: " + user)

              Users.insert(user)
              successfulAuthResponse(user)
            }  
        case _ => Forbidden ~> ResponseString("You need to add access_token params or login, password params")
      }
    case _ => MethodNotAllowed
  }
}


//in the future it will be separated project luna-war (also will be luna-netty)
class V1Filter extends unfiltered.filter.Planify(V1.intent)