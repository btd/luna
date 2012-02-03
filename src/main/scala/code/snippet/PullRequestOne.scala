/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package code.snippet

import net.liftweb._
import http._
import common._
import util._
import Helpers._
import code.model._
import code.snippet.SnippetHelper._
import bootstrap.liftweb._
import xml._
import Utility._

class PullRequestOneOps(pr: WithPullRequest) extends Loggable {


  def renderHelp = w(pr.pullRequest){pullRequest =>
    ".shell *" #> ("$ git remote add %s %s\n" +
      "$ git fetch %s\n" +
      "$ git merge %s/%s\n").format(pullRequest.srcRepoId.obj.get.owner.login.get,
        pullRequest.srcRepoId.obj.get.publicGitUrl,
        pullRequest.srcRepoId.obj.get.owner.login.get,
        pullRequest.srcRepoId.obj.get.owner.login.get,
        pullRequest.srcRef.get      )
  }

  def renderAll = pr.pullRequest match {
    case Full(pullRequest) => {
      val destHistory = pullRequest.destRepoId.obj.get.git.log(pullRequest.destRef.get).toList.reverse
      val srcHistory = pullRequest.srcRepoId.obj.get.git.log(pullRequest.srcRef.get).toList.reverse

      val diff = srcHistory.diff(destHistory)

      val diffCount = diff.size // bad

      if(!diff.isEmpty)

        ".commit *" #> diff.map(lc =>
          ".commit_msg *" #> <span>{lc.getFullMessage.split("\n").map(m => <span>{m}</span><br/>)}</span> &
          ".commit_author *" #> (lc.getAuthorIdent.getName + " at " + SnippetHelper.timeFormatter.format(lc.getAuthorIdent.getWhen))
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
    case _ => PassThru
  }

  def renderForm = w(pr.pullRequest){pullRequest => {
      "p" #> <p>{pullRequest.description.get}</p> &
      "button" #> (if (!pullRequest.accepted_?.get && UserDoc.loggedIn_?) 
                    SHtml.button("Close", processPullRequestClose(pullRequest), "class" -> "button") 
                  else NodeSeq.Empty)
    }
  }

  def processPullRequestClose(pullRequest: PullRequestDoc)() = {
    pullRequest
      .accepted_?(true)
      .srcRef(pullRequest.srcRepo.git.resolve(pullRequest.srcRef.get))
      .destRef(pullRequest.destRepo.git.resolve(pullRequest.destRef.get))
      .save
  }

}