/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
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

/**
 * User: denis.bardadym
 * Date: 10/5/11
 * Time: 2:14 PM
 */

class CommitOps(c: WithCommit) {

  def renderSourceTreeDefaultLink = w(c.repo)(renderSourceTreeLink(_, Full(c.commit)))

  def renderCommitsDefaultLink = w(c.repo)(renderCommitsLink(_, Full(c.commit)))

  def renderBranchSelector = w(c.repo){repo => 
    ".current_branch" #>
          SHtml.ajaxSelect(repo.git.branches.zip(repo.git.branches),
            if(repo.git.branches.contains(c.commit)) Full(c.commit) else Empty,
            value => S.redirectTo(repo.commitsUrl(value)))
  }


  def renderCommitsList = w(c.repo){repo => 
        ".day *" #>  groupCommitsByDate(repo.git.log(c.commit)).map(p => {
        ".date *" #> p._1 &
        ".commit *" #> p._2.map(lc => {
          ".commit_msg *" #> <span>{lc.getFullMessage.split("\n").map(m => <span>{m}</span><br/>)}</span> &
          ".commit_author *" #> (lc.getAuthorIdent.getName + " at " + SnippetHelper.timeFormatter.format(lc.getAuthorIdent.getWhen)) &
          ".source_tree_link *" #> a(repo.sourceTreeUrl(lc.getName), Text("Source tree")) &
          ".diff_link *" #> a(repo.commitUrl(lc.getName), Text(lc.getName))
          
      })
    })
      
  }

  def renderDiffList =  w(c.repo){repo => {
          val diff = repo.git.diff(c.commit)

          val diffCount = diff.size // bad

          ".blob *" #> (diff.zipWithIndex.map(d => 
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
        }
   }
}
