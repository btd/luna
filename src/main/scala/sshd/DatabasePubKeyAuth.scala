/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package sshd

import org.apache.sshd.server.PublickeyAuthenticator
import org.apache.sshd.server.session.ServerSession
import java.security.PublicKey
import sun.rmi.runtime.Log
import com.twitter.logging.Logger
import entity.{SshKey, Account}

/**
 * Created by IntelliJ IDEA.
 * User: denis.bardadym
 * Date: 8/4/11
 * Time: 12:14 PM
 * To change this template use File | Settings | File Templates.
 */

class DatabasePubKeyAuth extends PublickeyAuthenticator {
  private val log = Logger.get(this.getClass)
  /**
   * Check the validity of a public key.
   *
   * @param username the username
   * @param key the key
   * @param session the server session
   * @return a boolean indicating if authentication succeeded or not
   */
  def authenticate(username: String, key: PublicKey, session: ServerSession): Boolean = {
    log.info("User %s tried to authentificate", username)
    val keys = SshKey.byOwnerName(username)
    log.info("Founded %d keys", keys.length)
    keys.count(SshUtil.parse((_:SshKey)) == key) > 0
  }

}