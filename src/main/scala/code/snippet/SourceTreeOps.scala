/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package code.snippet

import bootstrap.liftweb.SourceTreePage
import net.liftweb._
import common._
import util.Helpers._
import code.model._
import util._
import xml.Text

/**
 * User: denis.bardadym
 * Date: 10/3/11
 * Time: 12:27 PM
 */

class SourceTreeOps(stp: SourceTreePage) extends Loggable {
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
          </div>{if (pageOwner_?(UserDoc.currentUser)) <a href={"/admin/" + repo.owner.login.get + "/" + repo.name} class="admin_button">
            <span class="ui-icon ui-icon-gear "/>
        </a>}
        </div>
      case _ => PassThru
    }
  }

  def renderTree = {

    stp.repo match {

      case Full(repo) => {
        val xhtmlSourceEntiries = repo.ls_tree(stp.path).flatMap(se => se match {
                case Tree(path, _) => {
                  <tr class="tree">
                    <td><a href={repo.homePage + (stp.path match {
                      case Nil => "/"
                      case l => l.mkString("/", "/", "/")
                    }) + path}>{path}/</a></td>
                    <td>Date</td>
                    <td>Commit message</td>
                  </tr>
                }
                case Blob(path, _) => {
                  <tr class="blob">
                    <td>{path}</td>
                    <td>Date</td>
                    <td>Commit message</td>
                  </tr>
                }
              })
        val parentEntry =
          <tr class="tree">
            <td><a href={repo.homePage + stp.path.dropRight(1).mkString("/")}>..</a></td>
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
            "#breadcrumbs *" #> (<a href={repo.homePage}>{repo.name.get}</a> ++ stp.path.zipWithIndex.flatMap(a =>
      Text("/") ++ (if (stp.path.size -2 == a._2) <a href={repo.homePage + "/" + stp.path.dropRight(stp.path.size - a._2 - 1).mkString("/")}>{a._1}</a> else <span>{a._1}</span>)) )
          }

          case _ => PassThru
        }


  }
}