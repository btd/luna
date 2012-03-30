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

class DatabasePubKeyAuth extends PublickeyAuthenticator with Loggable {


  /**
   * Check the validity of a public key.
   *
   * @param username the username
   * @param key the key
   * @param session the server session
   * @return a boolean indicating if authentication succeeded or not
   */
  def authenticate(username: String, key: PublicKey, session: ServerSession): Boolean = {
    //logger.debug("User " + username + " tried to authentificate")
    UserDoc.byName(username) match {
      case Some(u) if u.suspended.get => false
      case Some(u) => {
        tryo {
          val keys = (u.keys ++ u.repos.flatMap(_.keys))
                .filter(SshUtil.parse((_: SshKeyBase[_])) == key)
          session.setAttribute(DatabasePubKeyAuth.SSH_KEYS_KEY, keys)
          session.setAttribute(DatabasePubKeyAuth.USER_KEY, u)

          !keys.isEmpty
        } openOr {
          false
        }
      }
      case _ => false
    }
  }


}

object DatabasePubKeyAuth {
  val SSH_KEYS_KEY = new AttributeKey[Seq[SshKeyBase[_]]]
  val USER_KEY = new AttributeKey[UserDoc]
}

