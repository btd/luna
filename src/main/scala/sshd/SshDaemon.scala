/*
 * Copyright (c) 2011 Denis Bardadym
 * This project like github.
 * Distributed under Apache Licence.
 */

package sshd

import org.apache.sshd.SshServer
import org.apache.sshd.server.auth.UserAuthPublicKey
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider
import java.util.Arrays

/**
 * Created by IntelliJ IDEA.
 * User: denis.bardadym
 * Date: 8/1/11
 * Time: 11:37 AM
 * To change this template use File | Settings | File Templates.
 */

object SshDaemon {
  val port = 22
  private val sshd = SshServer.setUpDefaultServer()

  sshd.setPort(port)
  //TODO sshd.setKeyPairProvider(new FileKeyPairProvider(List("./ssh_host_rsa_key", "./ssh_host_dsa_key").toArray))
  sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider("keycert"))
  sshd.setUserAuthFactories(Arrays.asList(new UserAuthPublicKey.Factory))
  sshd.setPublickeyAuthenticator(new DatabasePubKeyAuth())
  sshd.setCommandFactory(new GitoChtoToCommandFactory())
  sshd.setShellFactory(new NoShell())

  def start() = sshd.start()

  def stop() = sshd.stop()

}