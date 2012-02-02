package notification.client

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
import json.Printer._


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
case class PushEvent(where: RepositoryDoc, who: Box[UserDoc], pusher: PersonIdent, what: () => List[RevCommit])

object NotifyActor extends LiftActor {
	lazy val notifyServerUrl = Props.get("notification.url")

	private val formatter = new java.text.SimpleDateFormat("yyyy.MM.dd HH:mm:ss")

	private def subscribers(t: NotifyEvents.Value, repo: RepositoryDoc) = {
		import com.foursquare.rogue.Rogue._

		for { subscription <- (NotifySubscriptionDoc where (_.repo eqs repo.id.get) and (_.onWhat eqs t) fetch) 
						user <- subscription.who.obj} 
				yield {ActorLogger.debug(subscription); (user, subscription.output.get)}
	}

	implicit def asJValue(pi: PersonIdent): JValue = {
		if(pi == null) JObject(Nil)
		else JObject(
				JField("name", pi.getName) :: 
				JField("email", pi.getEmailAddress) :: 
				Nil)
	}

	implicit def asJValue(cl: List[RevCommit]): JValue = {
		import org.joda.time.DateTime

		JArray(cl.map(commit => 
			JObject(
				JField("message", commit.getFullMessage) :: 
				JField("author", commit.getAuthorIdent) :: 
				JField("date", (new DateTime(commit.getAuthorIdent.getWhen)).toString) :: 
				Nil).asInstanceOf[JValue]))
		}
	

	def messageHandler = {
		case PushEvent(repo, user, ident, commitSeq) => 
			notifyServerUrl.map(urlAddress => {

				val subs = subscribers(NotifyEvents.Push, repo)
				ActorLogger.debug("Subscribers " + subs)
				if(!subs.isEmpty) {
					ActorLogger.debug("Found subscribers")
	
					for((user, output) <- subs) {
						val outputJson = 

						h((url(urlAddress + "/push") <<< compact(render(
							("services", output.asJValue) ~
							("repository", repo.asJValue) ~
							("commits", commitSeq()) ~
							("gitPusher", ident)
						))) >|)
					}
				}
				
			})			
	}

	lazy val h = new nio.Http

	def onShutdown() = {//TODO add to LiftRules
		h.shutdown
	}


}