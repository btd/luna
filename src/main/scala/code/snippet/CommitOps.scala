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
import common._
import http._
import util.Helpers._
import SnippetHelper._
import bootstrap.liftweb._
import util._
import xml._
import Utility._
import code.model._
import code.lib._
import Sitemap._

/**
 * User: denis.bardadym
 * Date: 10/5/11
 * Time: 2:14 PM
 */

class CommitOps(se: SourceElement) {

  def renderSourceTreeDefaultLink = {
    val commit = (if(se.repo.git.inited_?) Full(se.commit) else Empty) 
    renderSourceTreeLink(se.repo, commit)
  }

  def renderCommitsDefaultLink = {
    val commit = (if(se.repo.git.inited_?) Full(se.commit) else Empty) 
    renderCommitsLink(se.repo, commit)
  }

  def renderPullRequestsDefaultLink: NodeSeq => NodeSeq = { renderPullRequestsLink(se.repo) }

  def renderBranchSelector = 
    if(se.pathLst.isEmpty)
      ".current_branch" #>
          SHtml.ajaxSelect(se.repo.git.branches.zip(se.repo.git.branches),
            if(se.repo.git.branches.contains(se.commit)) Full(se.commit) else Empty,
            value => S.redirectTo(historyAtCommit.calcHref(SourceElement.rootAt(se.repo,value))))
    else cleanAll
  


  def renderCommitsList = 
      ".day *" #>  groupCommitsByDate(se.repo.git.log(se.commit, se.pathLst)).map(p => {
        ".date *" #> p._1 &
        ".commit *" #> p._2.map(lc => {
          ".commit_msg *" #> <span>{lc.getFullMessage.split("\n").map(m => <span>{m}</span><br/>)}</span> &
          ".commit_author *" #> (lc.getAuthorIdent.getName + " at " + timeFormat(lc.getAuthorIdent.getWhen)) &
          {
            se match {
              case t @ Tree(_, _, _) =>
                ".source_tree_link *" #> a(treeAtCommit.calcHref(t), Text("tree")) &
                ".diff_link *" #> a(commit.calcHref(t.copy(commit = lc.getName)), Text(lc.getName))
              case b @ Blob(_, _, _, _) =>
                ".source_tree_link *" #> a(blobAtCommit.calcHref(b), Text("blob")) &
                ".diff_link *" #> a(commit.calcHref(b.copy(commit = lc.getName)), Text(lc.getName))
            } 
          }
        })
      })
  

  def renderDiffList =  {
    val diff = se.repo.git.diff(se.commit + "^1", se.commit, Some(se.pathLst.mkString("/")))

    val diffCount = diff.size // bad

    ".blob *" #> diff.zipWithIndex.map(d => 
        ".blob_header [id]" #> ("diff" + d._2) &
          ".source_code" #> d._1._2 & 
        ".blob_header *" #> (d._1._1 match {
            case  Added(p ) => (".status [class+]" #> "new" & ".status *" #> p)
            case  Deleted(p) => (".status [class+]" #> "deleted" & ".status *" #> p)
            case  Modified(p) => (".status [class+]" #> "modified" & ".status *" #> p)
            case  Copied(op, np) => (".status [class+]" #> "modified" & ".status *" #> (op + " -> " + np))
            case  Renamed(op, np) => (".status [class+]" #> "modified" & ".status *" #> (op + " -> " + np))
        }) &
        ".prev [href]" #> (if(0 <= d._2 - 1)"#diff" + (d._2 - 1) else "") &
        ".next [href]" #> (if(diffCount > d._2 + 1)"#diff" + (d._2 + 1) else "") 
    )
      
   }
}
