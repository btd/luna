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
package notification.client

import net.liftweb._
import util._
import common._

import actor.{LiftActor, ActorLogger}

import code.model._
import NotifyEvents._

import dispatch._

import json.JsonDSL._
import json.JsonAST._
import json.Printer._

import org.bson.types.ObjectId


case class Msg(event: NotifyEvents.Value, repo: ObjectId, content: JValue)

object NotifyActor extends LiftActor {
	lazy val notifyServerUrl = Props.get("notification.url")

	private def subscribers(repoId: ObjectId) = {
		import com.foursquare.rogue.Rogue._

		for { subscription <- (NotifySubscriptionDoc where (_.repo eqs repoId) fetch) 
				user <- subscription.who.obj} 
		yield {
			ActorLogger.debug(subscription) 
			(user, subscription.output.get)
		}
	}

	private val eventToAddress = Map(
		NotifyEvents.Push -> "/push", 
		NotifyEvents.PullRequestOpen -> "/pull-request/open", 
		NotifyEvents.PullRequestClose -> "/pull-request/close")

	def messageHandler = {
		case Msg(eventType, repoId, content) =>
			for{
				urlAddress <- notifyServerUrl
				(user, output) <- subscribers(repoId)
			} {
				h((url(urlAddress + eventToAddress(eventType)) <<< pretty(render(
								("services", output.asJValue) ~
								("content", content)
				))) >|)
			}

	}

	lazy val h = new nio.Http

	def onShutdown() = {
		h.shutdown
	}


}