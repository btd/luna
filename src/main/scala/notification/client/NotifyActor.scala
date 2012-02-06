package notification.client

import net.liftweb._
import util._
import common._

import actor._

import code.model._

import org.eclipse.jgit._
import revwalk.RevCommit
import lib._

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
case class PushEvent(where: RepositoryDoc, pusher: PersonIdent, what: collection.mutable.Map[String, Ref])

object NotifyActor extends LiftActor {
	lazy val notifyServerUrl = Props.get("notification.url")

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
		case PushEvent(repo, ident, advRefs) =>

			notifyServerUrl.map(urlAddress => {
				val oldRefs = advRefs.map(_._2).toList

				val newRefs = repo.git.refsHeads

				val newBranches = newRefs.diff(oldRefs)
				val deletedBranches = oldRefs.diff(newRefs)
				val changedBranches = oldRefs.intersect(newRefs)

				if(!newBranches.isEmpty || !deletedBranches.isEmpty || !changedBranches.isEmpty) {//we need to send something
					val startEndRefs: List[(Ref, Ref)] = changedBranches.map(r => 
							oldRefs.filter(_ == r).head -> newRefs.filter(_ == r).head)//it is bad thing (TODO use advRefs map representaition)

					val changedHistory = for((start, end) <- startEndRefs) yield 
							(start.getName, repo.git.log(start.getObjectId, end.getObjectId).toList)

					val subs = subscribers(NotifyEvents.Push, repo)

					ActorLogger.debug("New branches: " + newBranches)
					ActorLogger.debug("Deleted branches: " + deletedBranches)
					ActorLogger.debug("changedHistory: " + changedHistory)

					if(!subs.isEmpty) {
						ActorLogger.debug("Found subscribers")
		
						for((user, output) <- subs) {

							h((url(urlAddress + "/push") <<< compact(render(
								("services", output.asJValue) ~
								("repository", repo.asJValue) ~
								("newBranches", JArray(newBranches.map(r => 
									JString(r.getName).asInstanceOf[JValue]).toList)) ~
								("deletedBranches", JArray(deletedBranches.map(r => 
									JString(r.getName).asInstanceOf[JValue]).toList)) ~
								("changedHistory", changedHistory) ~
								("gitPusher", ident)
							))) >|)
						}
					}
				}
				/*val commits = commitSeq()
				if(!commits.isEmpty) {
					val subs = subscribers(NotifyEvents.Push, repo)
					ActorLogger.debug("Subscribers " + subs)
					
				}*/
				
			})			
	}

	lazy val h = new nio.Http

	def onShutdown() = {//TODO add to LiftRules
		h.shutdown
	}


}