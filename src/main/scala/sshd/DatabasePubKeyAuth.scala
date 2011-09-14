/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package sshd

import org.apache.sshd.server.PublickeyAuthenticator
import org.apache.sshd.server.session.ServerSession
import java.security.PublicKey
import entity.SshKey
import net.liftweb.common.Loggable

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
    val keys = SshKey.byOwnerLogin(username)
    logger.debug("Founded " + keys.length + " keys")
    keys.count(SshUtil.parse((_: SshKey)) == key) > 0
  }

}