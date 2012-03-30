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
import http.js.JE._
import http.js.jquery._
import http.js.{JsCmd, JsExp, JsMember}
import JqJE._
import JqJsCmds._
import http.{S, SHtml}

import util.Helpers._
import code.model._
import code.lib._
import util._
import java.text.SimpleDateFormat
import org.eclipse.jgit.revwalk.RevCommit
import java.util.{Calendar, Date}
import collection.mutable.ListBuffer
import xml.{Node, Text, NodeSeq}

import Sitemap._

/**
 * User: denis.bardadym
 * Date: 10/5/11
 * Time: 5:20 PM
 */

object SnippetHelper {


  def cleanAll: NodeSeq => NodeSeq = "*" #> NodeSeq.Empty

  def w[B](arg: Box[B])(f:(B) => (NodeSeq => NodeSeq)): NodeSeq => NodeSeq = arg match {
    case Full(a) => f(a)
    case _ => cleanAll
  }

  def button(text: String, onSubmit: () => Any): CssSel = 
    "button" #> SHtml.button(text, onSubmit, "class" -> "button")


  def suffix(list: List[String]) : String = suffix(list, "/", "")

   def suffix(list: List[String], first: String, last: String): String =
    list match {
      case Nil => ""
      case l => l.mkString(first, "/", last)
    }

  
  //def cloneButtonAppend(r: RepositoryDoc, user: UserDoc) : NodeSeq =
  //SHtml.a(() => jsExpToJsCmd(Jq(".repo_list") ~> JqAppend(urlBoxXhtml(r.git.clone(user), repoName _, cloneButtonAppend))), <span class="ui-icon ui-icon-shuffle "/>, "class" -> "admin_button")

  //def cloneButtonRedirect(r: RepositoryDoc, user: UserDoc) =
  //SHtml.a(() => S.redirectTo(r.git.clone(user).sourceTreeUrl), <span class="ui-icon ui-icon-shuffle "/>, "class" -> "admin_button")

  def makeFork(repo: RepositoryDoc, u: UserDoc)():JsCmd = {
    repo.git.clone(u)
    S.redirectTo(userRepos.calcHref(u))
  }


  
  def a(href: String, value: NodeSeq) = <a href={href}>{value}</a>

  private val dateFormatter = new SimpleDateFormat("MMM dd, yyyy")
  private val timeFormatter = new SimpleDateFormat("HH:mm:ss")

  def dateFormat(d: Date): String = dateFormatter.format(d)
  def timeFormat(d: Date): String = timeFormatter.format(d)

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
        if (lastDate == null) lastDate = c.getAuthorIdent.getWhen

        if (!eqDay(lastDate, c.getAuthorIdent.getWhen)) {
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

  import code.lib._
  import bootstrap.liftweb._

  def renderSourceTreeLink(repo:RepositoryDoc, branch: Box[String]) = 
    ".repo_menu_link *" #> (S.attr("current") match {
      case Full(_) => Text("Sources")
      case _ => {
        val href = branch.map(b => treeAtCommit.calcHref(SourceElement.rootAt(repo, b)))
          .openOr(defaultTree.calcHref(repo))
          a( href, Text("Sources"))
        } 
    })


  def renderCommitsLink(repo:RepositoryDoc, branch: Box[String]) = 
    ".repo_menu_link *" #> (S.attr("current") match {
      case Full(_) => Text("Commits")
      case _ => {
        val href = branch.map(b => historyAtCommit.calcHref(SourceElement.rootAt(repo, b)))
          .openOr(defaultCommits.calcHref(repo))
        a( href, Text("Commits"))
      }
    })

  def renderPullRequestsLink(repo: RepositoryDoc): CssSel = {
    val pullRequestCount = repo.pullRequests.filter(!_.accepted_?.get).size
    val text = {
      if(pullRequestCount == 0) Text("Pull requests")
      else Text("Pull requests (%d)" format pullRequestCount)
    }
    ".repo_menu_link *" #> (S.attr("current") match {
      case Full(_) => text
      case _ => a(pullRequests.calcHref(repo), text)
    })
  }

  case class JqParents(parentSelector: JsExp) extends JsExp with JsMember {
     def toJsCmd = "parents(" + parentSelector.toJsCmd + ")"
  }

  case class JqToggleClass(classes: JsExp) extends JsExp with JsMember {
     def toJsCmd = "toggleClass(" + classes.toJsCmd + ")"
  }

  case class JqFind(child: JsExp) extends JsExp with JsMember {
     def toJsCmd = "find(" + child.toJsCmd + ")"
  }

  case class JqRemoveAttr(attr: JsExp) extends JsExp with JsMember {
     def toJsCmd = "removeAttr(" + attr.toJsCmd + ")"
  }

    case class JqVal(attr: JsExp) extends JsExp with JsMember {
     def toJsCmd = "val(" + attr.toJsCmd + ")"
  }


  def cleanForm(formSelector: String): JsCmd = {
    Jq(formSelector) ~> JqFind("input:text, input:password, input:file, select") ~> JqVal("") &
    Jq(formSelector) ~> JqFind("input:radio, input:checkbox") ~> JqRemoveAttr("checked") ~> JqRemoveAttr("selected")
  }

  def updateToggleOpenLink(repo: RepositoryDoc): JsCmd = {
    repo.open_?(!repo.open_?.get).save
              //S.redirectTo(userRepos.calcHref(user))

    Jq("#" + repo.id.get.toString) ~> 
      JqToggleClass("public private") ~>
      JqFind(".toggle_open") ~> 
      JqHtml(SHtml.a(Text(if (repo.open_?.get) "make private" else "make public")) {
        updateToggleOpenLink(repo)
      })
  }
  

  def renderRepositoryBlock(repo: RepositoryDoc, 
                            user: UserDoc, 
                            repoName: (RepositoryDoc) => NodeSeq): CssSel = {
      ".repo [class+]" #> (if(repo.ownerId.get == user.id.get) 
                              if(repo.open_?.get) "public" else "private"
                           else "collaborated") &
      ".repo [id]" #> repo.id.get.toString &
      ".repo_name *" #> repoName(repo) &
      ".clone-url" #> (repo.cloneUrlsForCurrentUser.map(url => "a" #> a(url._1, Text(url._2)))) &
      (UserDoc.currentUser match {
        case Full(currentUser) if(repo.ownerId.get == currentUser.id.get) => 
          ".fork *" #> SHtml.a(makeFork(repo, currentUser) _, Text("fork it")) &
          ".notification_page *" #> a(notification.calcHref(repo), Text("notify")) &
          ".toggle_open *" #> SHtml.a(Text(if (repo.open_?.get) "make private" else "make public")) {
            updateToggleOpenLink(repo)
          } &
          ".admin_page *" #> a(repoAdmin.calcHref(repo), Text("admin")) &
          repo.forkOf.obj.map(fr => 
                ".origin_link *" #> a(defaultTree.calcHref(fr), Text("origin")))
                .openOr(".origin_link" #> NodeSeq.Empty) 
        
        case Full(currentUser) =>
          ".fork *" #> SHtml.a(makeFork(repo, currentUser) _, Text("fork it")) &
          ".notification_page *" #> a(notification.calcHref(repo), Text("notify")) &
          ".toggle_open" #> NodeSeq.Empty &
          ".admin_page" #> NodeSeq.Empty &
          repo.forkOf.obj.map(fr => 
                ".origin_link *" #> a(defaultTree.calcHref(fr), Text("origin")))
                .openOr(".origin_link" #> NodeSeq.Empty)
        
        case _ => repo.forkOf.obj.map(fr => 
                ".origin_link *" #> a(defaultTree.calcHref(fr), Text("origin")) &
                ".toggle_open" #> NodeSeq.Empty &
                ".admin_page" #> NodeSeq.Empty &
                ".notification_page" #> NodeSeq.Empty &
                ".fork" #> NodeSeq.Empty)
                .openOr(".admin" #> NodeSeq.Empty)             
      })
  }

}