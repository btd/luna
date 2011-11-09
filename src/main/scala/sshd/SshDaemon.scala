/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package sshd

import org.apache.sshd.SshServer
import org.apache.sshd.server.auth.UserAuthPublicKey
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider
import org.apache.sshd.common.compression.CompressionNone
import org.apache.sshd.server.channel.{ChannelDirectTcpip, ChannelSession}
import java.util.{Collections, Arrays}
import org.apache.mina.core.session.IoSession
import org.apache.sshd.server.session.SessionFactory
import org.apache.mina.transport.socket.SocketSessionConfig

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
  sshd.setReuseAddress(true)
  sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider("keycert"))
  sshd.setUserAuthFactories(Arrays.asList(new UserAuthPublicKey.Factory))
  sshd.setPublickeyAuthenticator(new DatabasePubKeyAuth())
  sshd.setCommandFactory(new GitoChtoToCommandFactory())
  sshd.setShellFactory(new NoShell())
  sshd.setCompressionFactories(Arrays.asList(new CompressionNone.Factory()));
  sshd.setChannelFactories(Arrays.asList(new ChannelSession.Factory(), new ChannelDirectTcpip.Factory()));
  sshd.setSubsystemFactories(Collections.emptyList());
  sshd.setSessionFactory(new SessionFactory() {
    override def createSession(ioSession: IoSession) = {
      if (ioSession.getConfig().isInstanceOf[SocketSessionConfig]) {
        val c = ioSession.getConfig().asInstanceOf[SocketSessionConfig];
        c.setKeepAlive(true);
      }

      super.createSession(ioSession)
    }
  })

  def start() = sshd.start()

  def stop() = sshd.stop()

}