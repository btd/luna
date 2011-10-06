/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package code.snippet

import net.liftweb._
import common._
import http.js.JE.JsRaw
import http.{S, SHtml}

import util.Helpers._
import code.model._
import util._
import SnippetHelper._
import bootstrap.liftweb.UserRepoCommitPage
import xml.{NodeSeq, Text}
import java.util.Date

/**
 * User: denis.bardadym
 * Date: 10/5/11
 * Time: 2:14 PM
 */

class RepoCommitOps(urp: UserRepoCommitPage) {
   def renderCommitsList = {
     urp.repo match {
       case Full(repo) => {
         ".commits_list *" #> groupCommitsByDate(repo.git.log(urp.commit)).map(p => {
           <div class="day">
             <h3 class="date">{p._1}</h3>{
              p._2.map(lc => {
                <div class="commit">
                  <pre class="commit_msg">{lc.getFullMessage}</pre>

                  <p class="commit_author">{lc.getAuthorIdent.getName} at {SnippetHelper.timeFormatter.format(lc.getAuthorIdent.getWhen)}</p>
                </div>
              })
             }

             </div>
         })
       }
       case _ => PassThru
     }
   }


   def renderBranchSelector = branchSelector(urp.repo, _ => urp.commit, _.commitsUrl(_))
}