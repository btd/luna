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
import SnippetHelper._

/**
 * User: denis.bardadym
 * Date: 10/3/11
 * Time: 12:27 PM
 */

class SourceTreeOps(stp: SourcePage) extends Loggable {

  def renderUrlBox = urlBox(stp.repo, (r:RepositoryDoc) => Text(r.name.get))

  def renderTree = {

    stp.repo match {

      case Full(repo) => {
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

  def renderBranches = branchSelector(stp.repo, _ => stp.commit, _.sourceTreeUrl(_)+suffix(stp.path))
}