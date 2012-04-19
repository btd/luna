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
package notification


import net.liftweb.actor.LiftActor

import client.{NotifyActor, Msg}

import code.model._

object ActivityActor extends LiftActor {
	def messageHandler = {
		case PushEvent(id, ident, oldRefs) =>
         for{repo <- RepositoryDoc.byId(id)} {
            val newRefs = repo.git.refsHeads.map(ref => (ref.getName, ref)).toMap

            val (changedBranchesOld, deletedBranches) = oldRefs.partition(r => newRefs.contains(r._1))//(changed, deleted)
            val (changedBranchesNew, newBranches) = newRefs.partition(r => oldRefs.contains(r._1))//(changed, new)       

            val changedHistory = 
               for{(s, oldRef) <- changedBranchesOld
                  newRef <- changedBranchesNew.get(s)
                  if(oldRef.getObjectId != newRef.getObjectId)
               } yield {
                 val commits = repo.git.log(oldRef.getObjectId, newRef.getObjectId).toList

                 val commitDocs: List[CommitDoc] = commits.map { c =>
                     val ident = c.getAuthorIdent
                     CommitDoc
                        .when(ident.getWhen)
                        .msg(c.getFullMessage)
                        .ident(IdentDoc.name(ident.getName).email(ident.getEmailAddress))
                 }

                 ChangedBranchDoc.name(s).commits(commitDocs)
               }

            val push = PushEventDoc
                        .repo(id)
                        .added(newBranches.keys.toList)
                        .deleted(deletedBranches.keys.toList)
                        .changed(changedHistory.toList)

            ident.foreach { user =>
               push.who(user)
            }

            push.save

            NotifyActor ! Msg(NotifyEvents.Push, id, push.asJValue)
         }

	}
}