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
import code.model.{UserDoc, PullRequestDoc, RepositoryDoc}
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


      ".commits_list *" #> diff.map(lc =>
        <div class="commit">
          <pre class="commit_msg">{escape(lc.getFullMessage)}</pre>

          <p class="commit_author">
            {lc.getAuthorIdent.getName}
            at
            {SnippetHelper.timeFormatter.format(lc.getAuthorIdent.getWhen)}
          </p>


        </div>
      ) &
      ".blob *" #> pullRequest.srcRepoId.obj.get.git.diff(diff.head.getName + "^1", diff.last.getName)._2.map(
        d => ".source_code" #> escape(d)
      )
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

  def processPullRequestClose(pullRequest: PullRequestDoc)() =  pullRequest.accepted_?(true).save

}