/*
 * Copyright (c) 2011 Denis Bardadym
 * This project like github.
 * Distributed under Apache Licence.
 */

package sshd

import org.apache.sshd.server.PublickeyAuthenticator
import org.apache.sshd.server.session.ServerSession
import java.security.PublicKey

/**
 * Created by IntelliJ IDEA.
 * User: denis.bardadym
 * Date: 8/4/11
 * Time: 12:14 PM
 * To change this template use File | Settings | File Templates.
 */

class DatabasePubKeyAuth extends PublickeyAuthenticator {
  /**
   * Check the validity of a public key.
   *
   * @param username the username
   * @param key the key
   * @param session the server session
   * @return a boolean indicating if authentication succeeded or not
   */
  def authenticate(username: String, key: PublicKey, session: ServerSession): Boolean = {
    //TODO в первую очередь
    println("User: " + username)
    println("Key: " + key.getAlgorithm)
    true
  }

}