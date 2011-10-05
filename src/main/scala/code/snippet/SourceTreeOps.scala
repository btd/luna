/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package code.snippet

import bootstrap.liftweb.SourcePage
import net.liftweb._
import common._
import http.js.JE.JsRaw
import http.{S, SHtml}

import util.Helpers._
import code.model._
import util._
import xml.Text

/**
 * User: denis.bardadym
 * Date: 10/3/11
 * Time: 12:27 PM
 */

class SourceTreeOps(stp: SourcePage) extends Loggable {
  def pageOwner_?(user: Box[UserDoc]): Boolean = user match {
    case Full(u) if u.login.get == stp.userName => true
    case _ => false
  }


  def renderUrlBox = {
    stp.repo match {
      case Full(repo) => ".repo_block" #>
        <div class="repo_block">
          <h3>
            {repo.name.get}
          </h3>
          <div class="url-box">
            <ul class="clone-urls">
              {if (repo.canPush_?(UserDoc.currentUser)) <li class="private_clone_url">
              <a href={repo.privateSshUrl}>Ssh</a>
            </li>}<li class="public_clone_url selected">
              <a href={repo.publicGitUrl}>Git</a>
            </li>
            </ul>
              <input type="text" class="textfield" readonly=" " value={repo.publicGitUrl}/>
          </div>{if (pageOwner_?(UserDoc.currentUser)) <a href={"/admin" + repo.homePageUrl} class="admin_button">
            <span class="ui-icon ui-icon-gear "/>
        </a>}
        </div>
      case _ => PassThru
    }
  }

  def renderTree = {

    stp.repo match {

      case Full(repo) => {
        val xhtmlSourceEntiries = repo.git.ls_tree(stp.path, stp.commit).flatMap(se => se match {
                case Tree(path, _) => {
                  <tr class="tree">
                    <td><a href={repo.sourceTreeUrl(stp.commit) + (stp.path match {
                      case Nil => "/"
                      case l => l.mkString("/", "/", "/")
                    }) + path}>{path}/</a></td>
                    <td>Date</td>
                    <td>Commit message</td>
                  </tr>
                }
                case Blob(path, _) => {
                  <tr class="blob">
                    <td><a href={repo.sourceBlobUrl(stp.commit) + (stp.path match {
                      case Nil => "/"
                      case l => l.mkString("/", "/", "/")
                    }) + path}>{path}</a></td>
                    <td>Date</td>
                    <td>Commit message</td>
                  </tr>
                }
              })
        val parentEntry =
          <tr class="tree">
            <td><a href={repo.sourceTreeUrl(stp.commit) + (stp.path.dropRight(1) match {
                      case Nil => ""
                      case l => l.mkString("/", "/", "")
                    })}>..</a></td>
              <td></td>
                <td></td>
            </tr>
       "#source_tree *" #> (if(stp.path.isEmpty)
          xhtmlSourceEntiries
        else parentEntry :: xhtmlSourceEntiries)
      }

      case _ => PassThru
    }

  }

  def renderBreadcrumbs = {
    stp.repo match {

          case Full(repo) => {
            "#breadcrumbs *" #> (<a href={repo.sourceTreeUrl(stp.commit)}>{repo.name.get}</a> ++
              stp.path.zipWithIndex.flatMap(a =>
                  Text("/") ++
                    (if (stp.path.size -1  != a._2)
                      <a href={repo.sourceTreeUrl(stp.commit) + stp.path.dropRight(stp.path.size - a._2 - 1).mkString("/","/", "")}>{a._1}</a>
                    else
                      <span>{a._1}</span>)) )
          }

          case _ => PassThru
        }


  }

  def renderBranches = {
       stp.repo match {

          case Full(repo) => {
            //"#current_branch *" #> asScalaBuffer(branches).map(ref =>
            //  <option value={ref.getName.substring(ref.getName.lastIndexOf("/") + 1)} selected={if (stp.commit == ref.getName.substring(ref.getName.lastIndexOf("/") + 1)) "selected" else ""}>{ ref.getName.substring(ref.getName.lastIndexOf("/") + 1) } </option>
            //)
            "#current_branch" #>
              SHtml.ajaxSelect(repo.git.branches.zip(repo.git.branches),
                Full(stp.commit),
                value => S.redirectTo(repo.sourceTreeUrl( value) + (stp.path match {
                  case Nil => ""
                  case l => l.mkString("/", "/", "")
                })))

          }

          case _ => PassThru
        }

  }
}