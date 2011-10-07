/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package code.snippet

import net.liftweb._
import common._
import http.{S, SHtml}

import util.Helpers._
import code.model._
import util._
import xml.{Text, NodeSeq}
import java.text.SimpleDateFormat
import org.eclipse.jgit.revwalk.RevCommit
import java.util.{Calendar, Date}
import collection.mutable.ListBuffer

/**
 * User: denis.bardadym
 * Date: 10/5/11
 * Time: 5:20 PM
 */

object SnippetHelper extends Loggable{

  def branchSelector(repo: Box[RepositoryDoc], defaultSelectedBranch: RepositoryDoc => String, redirectUrl: (RepositoryDoc, String) => String) = {
    repo match {
      case Full(r) =>
        "#current_branch" #>
          SHtml.ajaxSelect(r.git.branches.zip(r.git.branches),
            Full(defaultSelectedBranch(r)),
            value => S.redirectTo(redirectUrl(r, value)))

      case _ => PassThru
    }
  }


  def suffix(list: List[String]) =
    list match {
      case Nil => ""
      case l => l.mkString("/", "/", "")
    }

  def urlBox(repo: Box[RepositoryDoc], name: RepositoryDoc => NodeSeq) = {
    repo match {
      case Full(rr) =>
        ".repo_block" #>
          <div class="repo_block">
            <h3>
              {name(rr)}
            </h3>
            <div class="url-box">
              <ul class="clone-urls">
                {rr.cloneUrls(UserDoc.currentUser).map(url => <li>
                {a(url._1, Text(url._2))}
              </li>)}
              </ul>
                <input type="text" class="textfield" readonly=" " value={rr.publicGitUrl}/>
            </div>{adminBox(repo, UserDoc.currentUser)}
          </div>
      case _ => PassThru
    }
  }

  def adminBox(repo: Box[RepositoryDoc], user: Box[UserDoc]) =
    repo match {
      case Full(r) if (r.owner_?(user)) => <a href={"/admin" + r.homePageUrl} class="admin_button">
          <span class="ui-icon ui-icon-gear "/>
      </a>
      case _ => NodeSeq.Empty
    }


  def a(href: String, value: NodeSeq) = <a href={href}>
    {value}
  </a>

  val dateFormatter = new SimpleDateFormat("MMM dd, yyyy")
  val timeFormatter = new SimpleDateFormat("HH:mm:ss")

  def eqDay(date1: Date, date2: Date) = {
    val c1 = Calendar.getInstance
    val c2 = Calendar.getInstance
    c1.setTime(date1)
    c2.setTime(date2)

    (c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)) && (c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR))
  }

  def groupCommitsByDate(commits: Iterator[RevCommit]) = {
    val groupedList = new ListBuffer[Pair[String, List[RevCommit]]]

    var lastDate: Date = null
    var commitsListAtLastDate = new ListBuffer[RevCommit]
    commits.foreach {
      c => {
        logger.debug("Begin process commit at " + c.getAuthorIdent.getWhen)
        if (lastDate == null) lastDate = c.getAuthorIdent.getWhen

        if (!eqDay(lastDate, c.getAuthorIdent.getWhen)) {
          logger.debug("Not the same date ")
          groupedList += dateFormatter.format(lastDate) -> commitsListAtLastDate.toList

          lastDate = c.getAuthorIdent.getWhen
          commitsListAtLastDate = new ListBuffer[RevCommit]
        }
        commitsListAtLastDate += c
      }
    }
    groupedList += dateFormatter.format(lastDate) -> commitsListAtLastDate.toList
    groupedList.toList
  }



//  def repoMenu(menu: Seq[Pair[String, String]], current: Pair[String, String])

}