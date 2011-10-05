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
import com.sun.org.apache.xerces.internal.impl.dv.xs.FullDVFactory
import xml.{Text, NodeSeq}


/**
 * User: denis.bardadym
 * Date: 10/5/11
 * Time: 5:20 PM
 */

object SnippetHelper {

  def branchSelector(repo: Box[RepositoryDoc], defaultSelectedBranch: String, redirectUrl: (RepositoryDoc, String) => String) = {
    repo match {
      case Full(r) =>
        "#current_branch" #>
          SHtml.ajaxSelect(r.git.branches.zip(r.git.branches),
            Full(defaultSelectedBranch),
            value => S.redirectTo(redirectUrl(r, value)))

      case _ => PassThru
    }
  }


  def suffix(list: List[String]) =
    list match {
      case Nil => ""
      case l => l.mkString("/", "/", "")
    }

  def urlBox(repo: Box[RepositoryDoc], name : RepositoryDoc => NodeSeq) = {
    repo match {
      case Full(rr) =>
        ".repo_block" #>
        <div class="repo_block">
          <h3>{name(rr)}</h3>
          <div class="url-box">
            <ul class="clone-urls">
              { rr.cloneUrls(UserDoc.currentUser).map( url => <li>{a(url._1, Text(url._2))}</li> ) }
            </ul>
            <input type="text" class="textfield" readonly=" " value={rr.publicGitUrl}/>
          </div>
          {adminBox(repo, UserDoc.currentUser)}
        </div>
      case _ => PassThru
    }
  }

  def adminBox(repo: Box[RepositoryDoc], user: Box[UserDoc]) =
  repo match {
    case Full(r) if (r.owner_?(user)) => <a href={"/admin" + r.homePageUrl} class="admin_button"><span class="ui-icon ui-icon-gear "/></a>
    case _ => NodeSeq.Empty
  }


  def a(href: String, value: NodeSeq) = <a href={href}>{value}</a>

}