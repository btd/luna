/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package sshd

import org.apache.sshd.server.PublickeyAuthenticator
import org.apache.sshd.server.session.ServerSession
import java.security.PublicKey
import net.liftweb.common.Loggable
import net.liftweb.util.Helpers._
import entity.{User}
import org.apache.sshd.common.Session.AttributeKey
import code.model.SshKeyDoc

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
    logger.debug("User " + username + " tried to authentificate")
    User.withLogin(username) match {
      case Some(u) => {
        tryo {
          val keys = u.keys.filter(SshUtil.parse((_: SshKeyDoc)) == key)
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
  val SSH_KEYS_KEY = new AttributeKey[Seq[SshKeyDoc]]
  val USER_KEY = new AttributeKey[User]
}

