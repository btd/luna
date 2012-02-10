package daemon.http

import javax.servlet.http.HttpServletRequest

import org.eclipse.jgit.transport.resolver.RepositoryResolver
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.http.server._
import resolver._
import org.eclipse.jgit.errors._


import net.liftweb.common._

import daemon.Resolver

import code.model.UserDoc

class LunaGitServlet extends GitServlet with RepositoryResolver[HttpServletRequest] with Resolver with Loggable {

	setAsIsFileService(AsIsFileService.DISABLED)

	setRepositoryResolver(this)

	def open(req: HttpServletRequest, path: String): Repository = {
		logger.debug("try to open repository by path: " + path)

		repoByPath(path, UserDoc byName (req.getRemoteUser)) match {
			case Some(repoDoc) => repoDoc.git.fs_repo_!
			case _ => throw new RepositoryNotFoundException(path)
		}

	}

	logger.debug("Http started")

}