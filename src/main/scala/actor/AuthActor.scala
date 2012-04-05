package actors

import akka.actor._
import Actor._

import akka.pattern.{ ask, pipe }

import akka.event.Logging

import daemon.sshd.SshUtil

import code.model._

case class PublicKeyCred(userName: String, publicKey: java.security.PublicKey)

case class GetUser(userName: String) 

case class Auth(publicKey: java.security.PublicKey)

class AuthActor extends Actor {
	import context._
	val log = Logging(system, this)
	
	def receive = {
		case PublicKeyCred(userName, publicKey) =>
			log.info("get request to login %s".format(userName))
			context.actorOf(Props[UsersActor]) ! GetUser(userName)
			val senderOrig = sender
			become {
				case userActor: ActorRef => 
					userActor ! Auth(publicKey)
					become {
						case successFullAuthActor: ActorRef => 
							log.info("succsessfully")
							senderOrig ! successFullAuthActor
					}

				case errorMsg: String => 
					log.info("get an error: %s".format(userName))
					unbecome()
					senderOrig ! errorMsg
			}
		case _ => log.error("unhandled message")
	}
}

class UsersActor extends Actor {
	import context._
	val log = Logging(system, this)

	def receive = {
		case GetUser(userName) =>
			log.info("try to find %s".format(userName))
			UserDoc.byName(userName) match {
				case Some(user) if user.suspended.get => 
					sender ! "User is suspended"
				case Some(user) => 
					sender ! context.actorOf(Props(new UserActor(user)), name = userName)

				case _ => sender ! "User is not founded"
			}
			
	}

}

class UserActor(user: UserDoc) extends Actor {

	def receive = {
		case Auth(publicKey) => 
			val keys = (user.keys ++ user.repos.flatMap(_.keys))
						.filter(SshUtil.parse((_: SshKeyBase[_])) == publicKey)

			sender ! context.actorOf(Props(new PublicKeyAuthedUserActor(user, keys)))
	}
}

case class CanUseRepository(path: String)

class PublicKeyAuthedUserActor(user: UserDoc, keys: Seq[SshKeyBase[_]]) extends Actor with daemon.Resolver {
	import context._
	val log = Logging(system, this)

	def receive = {
		case CanUseRepository(path) => 
			repoByPath(path, Some(user)) match {
				case Some(repo) if can_use_?(repo) => sender ! repo.id.get //TODO: for now it is ok, in future return path in FS 

				case Some(repo) => sender ! "%s tried to access to %s/%s".format(user.login.get, repo.owner.login.get, repo.name.get)

				case _ => sender ! "Repository not founded"
			}

		case msg => 
			log.error("unhandled message %s".format(msg.toString))
	}

	def can_use_?(r: RepositoryDoc) = {
	    if(user.id.get == r.ownerId.get) { // user@server:repo 
	      !keys.filter(_.acceptableFor(r)).isEmpty
	    } else {// cuser@server:user/repo
	      !r.collaborators.filter(_.login.get == user.login.get).isEmpty
	    }
	  }

}