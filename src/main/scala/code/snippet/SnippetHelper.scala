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

object SnippetHelper {


  def cleanAll: NodeSeq => NodeSeq = "*" #> NodeSeq.Empty

  def w[B](arg: Box[B])(f:(B) => (NodeSeq => NodeSeq)): NodeSeq => NodeSeq = arg match {
    case Full(a) => f(a)
    case _ => cleanAll
  }

  def button(text: String, onSubmit: () => Any): CssSel = "button" #> SHtml.button(text, onSubmit, "class" -> "button")


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
    S.redirectTo(u.homePageUrl)
  }

  def toggleOpen(repo: RepositoryDoc)():JsCmd = {
    repo.open_?(!repo.open_?.get).save
    S.redirectTo(repo.owner.homePageUrl)
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



}