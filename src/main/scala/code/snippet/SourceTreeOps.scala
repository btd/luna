/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package code.snippet

import bootstrap.liftweb.SourceElementPage
import net.liftweb._
import common._
import http._

import js.jquery.JqJE.Jq._
import js.jquery.JqJE.JqAppend._
import js.jquery.JqJsCmds._
import js.JsCmds
import js.JsExp._
import util.Helpers._
import code.model._
import util._
import xml.Text
import SnippetHelper._

/**
 * User: denis.bardadym
 * Date: 10/3/11
 * Time: 12:27 PM
 */

class SourceTreeOps(stp: SourceElementPage) extends Loggable {

  def renderCommitsLink = stp.repo match {
       case Full(repo) => ".repo_menu *" #> a(repo.commitsUrl, Text("Commits"))
       case _ => PassThru
     }

   def repoName(r: RepositoryDoc) = {
    r.forkOf.obj match {
      case Full(rr) => Text(r.name.get + " clone of ") ++ a(rr.sourceTreeUrl, Text(rr.owner.login.get + "/" + rr.name.get))
      case _ =>  Text(r.name.get)
    }
  }

  def renderUrlBox = urlBox(stp.repo, repoName _ , cloneButtonRedirect)

  def renderTree = stp.repo match {
       case Full(repo) =>  {
        val xhtmlSourceEntiries = repo.git.ls_tree(stp.path, stp.commit).flatMap(se => se match {
                case Tree(path, _) => {
                  <tr class="tree">
                    <td><a href={repo.sourceTreeUrl(stp.commit) + suffix(stp.path) + "/" + path}>{path}/</a></td>
                    <td>Date</td>
                    <td>Commit message</td>
                  </tr>
                }
                case Blob(path, _) => {
                  <tr class="blob">
                    <td><a href={repo.sourceBlobUrl(stp.commit) + suffix(stp.path) + "/" + path}>{path}</a></td>
                    <td>Date</td>
                    <td>Commit message</td>
                  </tr>
                }
              })
        val parentEntry =
          <tr class="tree">
            <td><a href={repo.sourceTreeUrl(stp.commit) + suffix(stp.path.dropRight(1))}>..</a></td>
              <td></td>
              <td></td>
            </tr>
       "#source_tree *" #> (if(stp.path.isEmpty)
          xhtmlSourceEntiries
        else parentEntry :: xhtmlSourceEntiries)
      }
      case _ => PassThru
     }



  def renderBreadcrumbs = stp.repo match {
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




  def renderBranches = branchSelector(stp.repo, _ => stp.commit, _.sourceTreeUrl(_)+suffix(stp.path))
}