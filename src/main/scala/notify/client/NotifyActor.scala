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
case class PushEvent(where: RepositoryDoc, who: Box[UserDoc], pusher: PersonIdent, what: () => List[RevCommit])

object NotifyActor extends LiftActor {
	lazy val notifyServerUrl = Props.get("notify.url")

	private val formatter = new java.text.SimpleDateFormat("yyyy.MM.dd HH:mm:ss")

	private def subscribers(t: NotifyEvents.Value, repo: RepositoryDoc) = {
		import com.foursquare.rogue.Rogue._

		for { subscription <- (NotifySubscriptionDoc where (_.onWhat eqs t) and (_.repo eqs repo.id.get) fetch) 
						user <- subscription.who.obj} 
				yield {(user, subscription.output.get)}
	}

	implicit def asJValue(pi: PersonIdent): JValue =
		JObject(JField("name", pi.getName) :: JField("date", formatter.format(pi.getWhen)) :: JField("email", pi.getEmailAddress) :: Nil)

	implicit def asJValue(cl: List[RevCommit]): JValue =
		JArray(cl.map(commit => JObject(JField("msg", commit.getFullMessage) :: JField("author", commit.getAuthorIdent) :: Nil).asInstanceOf[JValue]))
	

	def messageHandler = {
		case PushEvent(repo, user, ident, commitSeq) => 
			notifyServerUrl.map(urlAddress => {
				logger.debug("Notify url is " + urlAddress)
				val subs = subscribers(NotifyEvents.Push, repo)
				if(!subs.isEmpty) {
					logger.debug("Found subscribers")
					for(subscriber <- subs) {
						subscriber._2 match {
							case NotifyOptions(Full(Email(emails))) => {
								h(:/(urlAddress) / "mail" / "push" <<< (
									("to", subscriber._1.email.get :: emails) ~
									("repo", repo.asJValue) ~ 
									("user", user.map(_.asJValue) openOr (JNothing)) ~
									("pusher", ident) ~ 
									("commits", commitSeq())).toString >|)
							}
							case _ =>
						}
					}
					
				}
				
			})			
	}
	

	lazy val h = new nio.Http

	def onShutdown() = {//TODO add to LiftRules
		h.shutdown
	}


}