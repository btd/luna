/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package sshd.git

import net.liftweb._
import util._
import Helpers._
import common._
import java.net.InetSocketAddress
import org.eclipse.jgit.transport.{Daemon, DaemonClient}
import org.eclipse.jgit.transport.resolver.RepositoryResolver
import code.model.{RepositoryDoc, UserDoc }
import json.JsonDSL._

/**
 * User: denis.bardadym
 * Date: 9/26/11
 * Time: 3:07 PM
 */

object GitDaemon {
  private lazy val gitDaemonBindAddress = new InetSocketAddress(Props.get("daemon.git.address", "localhost"), Daemon.DEFAULT_PORT)

  private lazy val daemon = new Daemon(gitDaemonBindAddress)

  def start = {
    daemon.setRepositoryResolver(MyRepositoryResolver)
    daemon.start
  }

  def stop = daemon.stop
}

object MyRepositoryResolver extends RepositoryResolver[DaemonClient] with Loggable {
  def open(req: DaemonClient, name: String) = {
    logger.debug("Get anonymous request %s %s".format(req.toString, name))
    name.split("/").toList match {
      case user :: repoName :: Nil => {
        tryo {
          UserDoc.find("login", user).get.repos.filter(_.name.get == repoName).head.git
        } openOr {
          null
        }
      }
      case _ => null
    }
  }
}