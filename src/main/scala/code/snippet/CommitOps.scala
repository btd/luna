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
import code.model._

/**
 * User: denis.bardadym
 * Date: 10/5/11
 * Time: 2:14 PM
 */

class CommitOps(c: WithCommit) {

  def renderBranchSelector = w(c.repo){repo => 
    ".current_branch" #>
          SHtml.ajaxSelect(repo.git.branches.zip(repo.git.branches),
            Full(c.commit),
            value => S.redirectTo(repo.commitsUrl(c.commit)))
  }


  def renderCommitsList = w(c.repo){repo => 
        ".day *" #>  groupCommitsByDate(repo.git.log(c.commit)).map(p => {
        ".date *" #> p._1 &
        ".commit *" #> p._2.map(lc => {
          ".commit_msg *" #> lc.getFullMessage &
          ".commit_author *" #> (lc.getAuthorIdent.getName + " at " + SnippetHelper.timeFormatter.format(lc.getAuthorIdent.getWhen)) &
          ".source_tree_link *" #> a(repo.sourceTreeUrl(lc.getName), Text("Source tree")) &
          ".diff_link *" #> a(repo.commitUrl(lc.getName), Text(lc.getName))
          
      })
    })
      
  }

  def renderDiffList =  w(c.repo){repo => {
          val diff = repo.git.diff(c.commit)

          ".diff_status_list *" #> (diff._1.map(
            _ match {
              case  Added(p ) => <li class="new">{p}</li>
              case  Deleted(p) => <li class="deleted">{p}</li>
              case  Modified(p) => <li class="modified">{p}</li>
              case  Copied(op, np) => <li class="modified">{op} -> {np}</li>
              case  Renamed(op, np) => <li class="modified">{op} -> {np}</li>
            }
          )) & 
          ".blob *" #> (diff._2.map(d => ".source_code" #> d) )
        }
   }
}
