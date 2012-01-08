/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package daemon.sshd

import org.apache.sshd.SshServer
import org.apache.sshd.server.auth.UserAuthPublicKey
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider
import org.apache.sshd.common.compression.CompressionNone
import org.apache.sshd.server.channel.{ChannelDirectTcpip, ChannelSession}
import java.util.{Collections, Arrays}
import org.apache.mina.core.session.IoSession
import org.apache.sshd.server.session.SessionFactory
import org.apache.mina.transport.socket.SocketSessionConfig


import net.liftweb.util.Props

import main.Constants

import daemon.Service

/**
 * Created by IntelliJ IDEA.
 * User: denis.bardadym
 * Date: 8/1/11
 * Time: 11:37 AM
 * To change this template use File | Settings | File Templates.
 */

object SshDaemon extends Service {
  val DEFAULT_PORT = 22
  private val sshd = SshServer.setUpDefaultServer

  def init() = {
    sshd.setPort(Props.getInt(Constants.SSHD_PORT_OPTION, DEFAULT_PORT))
    sshd.setReuseAddress(true)
    sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider("keycert"))//TODO move Props + Constants
    sshd.setUserAuthFactories(Arrays.asList(new UserAuthPublicKey.Factory))
    sshd.setPublickeyAuthenticator(new DatabasePubKeyAuth())
    sshd.setCommandFactory(new GitoChtoToCommandFactory())
    sshd.setShellFactory(new NoShell())
    sshd.setCompressionFactories(Arrays.asList(new CompressionNone.Factory()))
    sshd.setChannelFactories(Arrays.asList(new ChannelSession.Factory(), new ChannelDirectTcpip.Factory()))
    sshd.setSubsystemFactories(Collections.emptyList())
    sshd.setSessionFactory(new SessionFactory() {
      override def createSession(ioSession: IoSession) = {
        if (ioSession.getConfig().isInstanceOf[SocketSessionConfig]) {
          val c = ioSession.getConfig().asInstanceOf[SocketSessionConfig]
          c.setKeepAlive(true)
        }

        super.createSession(ioSession)
      }
    })

    sshd.start
  }

  def shutdown() = sshd.stop

}