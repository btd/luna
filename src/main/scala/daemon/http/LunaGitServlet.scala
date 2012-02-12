package daemon.http

import javax.servlet.http.HttpServletRequest

import org.eclipse.jgit.lib.PersonIdent
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.http.server._
import resolver._
import org.eclipse.jgit.errors._


import net.liftweb.common._

import daemon.Resolver

import code.model.UserDoc

import org.eclipse.jgit.transport.resolver.RepositoryResolver
import org.eclipse.jgit.transport.resolver.ReceivePackFactory
import org.eclipse.jgit.transport.ReceivePack

class LunaGitServlet extends GitServlet 
	with RepositoryResolver[HttpServletRequest] 
	with ReceivePackFactory[HttpServletRequest] with Resolver with Loggable {

	setAsIsFileService(AsIsFileService.DISABLED)

	setRepositoryResolver(this)
	setReceivePackFactory(this)

	def open(req: HttpServletRequest, path: String): Repository = {
		logger.debug("try to open repository by path: " + path)

		repoByPath(path, UserDoc byName (req.getAttribute("user").asInstanceOf[UserDoc].login.get)) match {
			case Some(repoDoc) => repoDoc.git.fs_repo_!
			case _ => throw new RepositoryNotFoundException(path)
		}

	}

	def create(req: HttpServletRequest, repo: Repository): ReceivePack = {
		val rp = new ReceivePack(repo)
		val user = req.getAttribute("user").asInstanceOf[UserDoc]
		rp.setRefLogIdent(new PersonIdent(user.login.get, user.email.get))

		rp
	}

	logger.debug("Http started")

}