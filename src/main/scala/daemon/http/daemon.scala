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
package daemon.http

import net.liftweb.common.Loggable

import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory
import org.jboss.netty.channel.{ChannelPipelineFactory, Channels, SimpleChannelHandler,ChannelHandlerContext, ExceptionEvent, MessageEvent}
import org.jboss.netty.handler.codec.http.{HttpServerCodec, HttpChunkAggregator, HttpRequest}
import org.jboss.netty.bootstrap.ServerBootstrap
import org.jboss.netty.util.CharsetUtil

import java.net.InetSocketAddress
import java.util.concurrent.Executors

import code.model.RepositoryDoc

object SmartHttpDaemon extends daemon.Service with Loggable {

  var inited = false

  def init() {
    val factory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool, Executors.newCachedThreadPool)
    
    val bootstrap = new ServerBootstrap(factory)

    bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
      def getPipeline() = {
      	val pipeline = Channels.pipeline

      	pipeline.addLast("codec", new HttpServerCodec )
      	pipeline.addLast("aggregator", new HttpChunkAggregator(5242880))
      	pipeline.addLast("authHandler", new SmartHttpMessageHandler )

      	pipeline
      }
    })

    //b.setOption("localAddress", new InetSocketAddress(8080))

    bootstrap.setOption("reuseAddress", true)
    bootstrap.setOption("child.tcpNoDelay", true)
    bootstrap.setOption("child.keepAlive", true)

    bootstrap.bind(new InetSocketAddress(80));


  }

  def shutdown() {}

  

  def repoUrlForCurrentUser(r: RepositoryDoc) = ""

}

class SmartHttpMessageHandler extends SimpleChannelHandler with Loggable {
  override def exceptionCaught(ctx: ChannelHandlerContext, e: ExceptionEvent) {
  	logger.error("Exception recieved in smart http daemon", e.getCause)
    ctx.getChannel.close
  }

  override def messageReceived(ctx: ChannelHandlerContext, e: MessageEvent) {
    val httpResponse = e.getMessage.asInstanceOf[HttpRequest]
    val json = httpResponse.getContent.toString(CharsetUtil.UTF_8)
    logger.debug(json)
  }
}