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
package code.snippet

import net.liftweb._
import http._
import common._
import util._
import Helpers._
import code.model._
import code.lib._
import SnippetHelper._
import xml._
import Utility._

class PullRequestOneOps(pullRequest: PullRequestDoc) extends Loggable {

  def renderSourceTreeDefaultLink = renderSourceTreeLink(pullRequest.destRepo, None)

  def renderCommitsDefaultLink = renderCommitsLink(pullRequest.destRepo, None)

  def renderPullRequestsDefaultLink: NodeSeq => NodeSeq = 
    renderPullRequestsLink(pullRequest.destRepo)


  def renderHelp = 
    ".shell *" #> ("$ git remote add %s %s\n" +
      "$ git fetch %s\n" +
      "$ git merge %s/%s\n").format(pullRequest.srcRepoId.obj.get.owner.login.get,
        daemon.git.GitDaemon.repoUrlForCurrentUser(pullRequest.srcRepoId.obj.get),
        pullRequest.srcRepoId.obj.get.owner.login.get,
        pullRequest.srcRepoId.obj.get.owner.login.get,
        pullRequest.srcRef.get      )
 

  def renderAll =  {
      val destHistory = pullRequest.destRepoId.obj.get.git.log(pullRequest.destRef.get).toList.reverse
      val srcHistory = pullRequest.srcRepoId.obj.get.git.log(pullRequest.srcRef.get).toList.reverse

      val diff = srcHistory.diff(destHistory)

      val diffCount = diff.size // bad

      if(!diff.isEmpty)

        ".commit *" #> diff.map(lc =>
          ".commit_msg *" #> <span>{lc.getFullMessage.split("\n").map(m => <span>{m}</span><br/>)}</span> &
          ".commit_author *" #> (lc.getAuthorIdent.getName + " at " + timeFormat(lc.getAuthorIdent.getWhen))
        ) &
        ".blob *" #> 
                (pullRequest.srcRepoId.obj.get.git.diff(diff.head.getName + "^1", diff.last.getName).zipWithIndex.map(d => 
                      (".blob_header [id]" #> ("diff" + d._2) &
                        ".source_code" #> d._1._2 & 
                      ".blob_header *" #> ((d._1._1 match {
                                                    case  Added(p ) => (".status [class+]" #> "new" & ".status *" #> p)
                                                    case  Deleted(p) => (".status [class+]" #> "deleted" & ".status *" #> p)
                                                    case  Modified(p) => (".status [class+]" #> "modified" & ".status *" #> p)
                                                    case  Copied(op, np) => (".status [class+]" #> "modified" & ".status *" #> (op + " -> " + np))
                                                    case  Renamed(op, np) => (".status [class+]" #> "modified" & ".status *" #> (op + " -> " + np))
                                                  }) &
                                    ".prev [href]" #> (if(0 <= d._2 - 1)"#diff" + (d._2 - 1) else "") &
                                    ".next [href]" #> (if(diffCount > d._2 + 1)"#diff" + (d._2 + 1) else "") ))
                  )) 
        else ".blob" #>  NodeSeq.Empty & ".commit *" #> "No commits. Update ref or close PR."
    }
    

  def renderForm = {
      "p" #> <p>{pullRequest.description.get}</p> &
      "button" #> (if (!pullRequest.accepted_?.get && pullRequest.destRepo.canPush_?(UserDoc.currentUser)) 
                    SHtml.button("Close", processPullRequestClose(pullRequest), "class" -> "button") 
                  else NodeSeq.Empty)
    }


  def processPullRequestClose(pullRequest: PullRequestDoc)() = {
    pullRequest
      .accepted_?(true)
      .srcRef(pullRequest.srcRepo.git.resolve(pullRequest.srcRef.get))
      .destRef(pullRequest.destRepo.git.resolve(pullRequest.destRef.get))
      .save
  }

}