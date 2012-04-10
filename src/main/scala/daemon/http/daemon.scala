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
import org.jboss.netty.channel._
import org.jboss.netty.handler.codec.http._
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

      	pipeline.addLast("decoder", new HttpRequestDecoder )
        pipeline.addLast("encoder", new HttpResponseEncoder )
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
  	val response = new DefaultHttpResponse(HTTP_1_1, OK)
    val request = e.getMessage.asInstanceOf[HttpRequest]
    val request = req.asInstanceOf[HttpServletRequest]
 	 22	
+        val response = res.asInstanceOf[HttpServletResponse]
 	 23	
+
 	 24	
+        val user = authPassed_?(request)
 	 25	
+
 	 26	
+    user match {
 	 27	
+      case Some(u) => {
 	 28	
+        val wrapped = new HttpServletRequestWrapper(request)
 	 29	
+        wrapped.setAttribute("username", u.login.get)
 	 30	
+        wrapped.setAttribute("email", u.email.get)
 	 31	
+          chain.doFilter(wrapped, response)
 	 32	
+      }
 	 33	
+      case _ => {
 	 34	
+        response.addHeader("WWW-Authenticate", "Basic realm=\"" + realmName + "\"")
 	 35	
+            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not authenticated")
 	 36	
+      }
 	 37	
+    }
  }

  def authPassed_?(request: HttpRequest): Option[UserDoc] = {
  	for {
  		header <- Option(request.getHeader("Authorization"))
  		if header.startsWith("Basic ")
  	} yield {
  		val (username, password) = extractAndDecodeHeader(header)
  		UserDoc.byName(username).filter(u => u.password.match_?(password))
  	}
  }

  def extractAndDecodeHeader(header: String): (String, String) = {
 	val base64Token = header.substring(6).getBytes("UTF-8")

 	val token = new String(Base64.decodeBase64(base64Token), "UTF-8")

 	val tokens = token.split(":")
 	tokens(0) -> tokens(1)
  }

}
