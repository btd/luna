package notify.client

import net.liftweb._
import util._
import common._

import actor._

import code.model._

import org.eclipse.jgit._
import revwalk.RevCommit
import lib.PersonIdent

import dispatch._

import json.JsonDSL._
import json.JsonAST._

object NotifyEvents extends Enumeration {
	val Push = Value

		
}

/*
	This event for git-recieve-pack command.
	where - this is repository where this command appear
	who - optional user (because we can push through specific ssh key)
	pusher - this will be who make a push from JGit identification
	what - this is func that get me a seq of commits (i do not what ask sender convert for me a RevWalk to Seq)
*/
case class PushEvent(where: RepositoryDoc, who: Box[UserDoc], pusher: PersonIdent, what: () => Seq[RevCommit])

object NotifyActor extends LiftActor {
	lazy val notifyServerUrl = Props.get("notify.url")

	private def subscribers(t: NotifyEvents.Value) = {
		import com.foursquare.rogue.Rogue._

		for { subscription <- (NotifySubscriptionDoc where (_.onWhat eqs t) fetch) 
						user <- subscription.who.obj} 
				yield {user}
	}

	def messageHandler = {
		case PushEvent(repo, user, ident, commitSeq) => 
			notifyServerUrl.map(urlAddress => {
				val subs = subscribers(NotifyEvents.Push)
				if(!subs.isEmpty) {
					h(:/(urlAddress) <<< (repo.asJValue).toString >|)
				}
				
			})			
	}
	

	lazy val h = new nio.Http

	def onShutdown() = {
		h.shutdown
	}


}