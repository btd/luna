/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package code.snippet

import net.liftweb._
import common._
import http.js.JE.Call
import http.js.jquery._
import http.js.JsCmd
import JqJE._
import JqJsCmds._
import http.{S, SHtml}

import util.Helpers._
import code.model._
import util._
import java.text.SimpleDateFormat
import org.eclipse.jgit.revwalk.RevCommit
import java.util.{Calendar, Date}
import collection.mutable.ListBuffer
import xml.{Node, Text, NodeSeq}

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


  def suffix(list: List[String]) : String = suffix(list, "/", "")

   def suffix(list: List[String], first: String, last: String): String =
    list match {
      case Nil => ""
      case l => l.mkString(first, "/", last)
    }

  def urlBox(repo: Box[RepositoryDoc], name: RepositoryDoc => NodeSeq, onClone: (RepositoryDoc, UserDoc) => NodeSeq)  = {
   repo match {
      case Full(rr) =>
        ".repo_block" #> urlBoxXhtml(rr, name, onClone)
      case _ => PassThru
    }
  }

  def urlBoxXhtml(rr: RepositoryDoc, name: RepositoryDoc => NodeSeq, onClone: (RepositoryDoc, UserDoc) => NodeSeq): NodeSeq = {
      <div class="repo_block">
            <h3>
              {name(rr)}
            </h3>
            <div class="url-box">
              <ul class="clone-urls">
                {
                rr.cloneUrls(UserDoc.currentUser).map(url =>
                <li>{a(url._1, Text(url._2))}</li>)
                }
              </ul>
                <input type="text" class="textfield" readonly=" " value={rr.publicGitUrl}/>
            </div>{adminBox(Full(rr), UserDoc.currentUser, onClone)}
          </div>
  }


  def adminBox(repo: Box[RepositoryDoc], user: Box[UserDoc], cloneButton: (RepositoryDoc, UserDoc) => NodeSeq) =
    <div class="repo_admin_buttons">
      {repo match {
      case Full(r) if (r.owner_?(user)) => {
        <a href={"/admin" + r.homePageUrl} class="admin_button">
          <span class="ui-icon ui-icon-gear "/>
        </a> ++ cloneButton(r, user.get)
      }
      case Full(r) if UserDoc.loggedIn_? => cloneButton(r, user.get)
      case _ => NodeSeq.Empty
    }  }

    </div>

  def cloneButtonAppend(r: RepositoryDoc, user: UserDoc) : NodeSeq =
  SHtml.a(() => jsExpToJsCmd(Jq(".repo_list") ~> JqAppend(urlBoxXhtml(r.git.clone(user), repoName _, cloneButtonAppend))), <span class="ui-icon ui-icon-shuffle "/>, "class" -> "admin_button")

  def cloneButtonRedirect(r: RepositoryDoc, user: UserDoc) =
  SHtml.a(() => S.redirectTo(r.git.clone(user).sourceTreeUrl), <span class="ui-icon ui-icon-shuffle "/>, "class" -> "admin_button")


  def repoName(r: RepositoryDoc) = {
    r.forkOf.obj match {

      case Full(rr) => a(r.sourceTreeUrl, Text(r.name.get)) ++ Text(" clone of ") ++ a(rr.sourceTreeUrl, Text(rr.owner.login.get + "/" + rr.name.get))
      case _ => a(r.sourceTreeUrl, Text(r.name.get))
    }
  }


  def a(href: String, value: NodeSeq) = <a href={href}>{value}</a>

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