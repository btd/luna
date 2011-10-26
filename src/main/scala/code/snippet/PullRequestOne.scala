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
import bootstrap.liftweb.{PullRequestRepoPage, RepoPage}
import xml.{NodeSeq, Text}

class PullRequestOneOps(pr: PullRequestRepoPage) extends Loggable {


  def renderAll = pr.pullRequest match {
    case Full(pullRequest) => {
      val destHistory = pullRequest.destRepoId.obj.get.git.log(pullRequest.destRef.get).toList.reverse
      val srcHistory = pullRequest.srcRepoId.obj.get.git.log(pullRequest.srcRef.get).toList.reverse

      val diff = srcHistory.diff(destHistory)


      ".commits_list *" #> diff.map(lc =>
        <div class="commit">
          <pre class="commit_msg">{lc.getFullMessage}</pre>

          <p class="commit_author">
            {lc.getAuthorIdent.getName}
            at
            {SnippetHelper.timeFormatter.format(lc.getAuthorIdent.getWhen)}
          </p>


        </div>
      ) &
      ".diff_list *" #> pullRequest.srcRepoId.obj.get.git.diff(diff.head.getName + "^1", diff.last.getName)._2.map(
        d => {
            <div class="source_code_holder">
            <pre>
              <code class="diff">{d}</code>
              </pre>
            </div>
          }
      )
    }
    case _ => PassThru
  }

  def renderForm = pr.pullRequest match {
    case Full(pullRequest) => {
      "p" #> <p>{pullRequest.description.get}</p> &
      "button" #> (if (!pullRequest.accepted_?.get) SHtml.button("Close", processPullRequestClose, "class" -> "button") else NodeSeq.Empty)

    }
    case _ => PassThru
  }

  def processPullRequestClose() = pr.pullRequest match {
      case Full(pullRequest) => {
      pullRequest.accepted_?(true).save
    }
    case _ => PassThru
  }

}