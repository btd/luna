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
package daemon.sshd

import org.apache.sshd.server.PublickeyAuthenticator
import org.apache.sshd.server.session.ServerSession
import java.security.PublicKey
import net.liftweb.common._
import net.liftweb.util.Helpers._
import org.apache.sshd.common.Session.AttributeKey
import code.model._

import actors.{AuthActor, PublicKeyCred}
import akka.actor.{Props => AProps, ActorRef}
import akka.pattern.ask
import akka.dispatch.Await
import akka.dispatch.Future
import akka.util.Timeout
import akka.util.duration._

import bootstrap.liftweb.Boot._

class DatabasePubKeyAuth extends PublickeyAuthenticator with Loggable {
  implicit val timeout = Timeout(5000)

  /**
   * Check the validity of a public key.
   *
   * @param username the username
   * @param key the key
   * @param session the server session
   * @return a boolean indicating if authentication succeeded or not
   */
  def authenticate(username: String, key: PublicKey, session: ServerSession): Boolean = {
    val future = system.actorOf(AProps[AuthActor]) ? PublicKeyCred(username, key)

    Await.result(future, timeout.duration) match { // TODO change to configuration property
      case error: String => 
        logger.error("Auth failed: %s".format(error))
        false

      case actor: ActorRef => 
        logger.info("Auth ok")
        session.setAttribute(DatabasePubKeyAuth.ACTOR_KEY, actor)
        true

      case other => 
        logger.warn("Got unrecognized answer: %s".format(other.toString))
        false
    }
  }


}

object DatabasePubKeyAuth {
  val ACTOR_KEY = new AttributeKey[ActorRef]
}

