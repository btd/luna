/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package code.snippet

import bootstrap.liftweb._
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
import xml._
import SnippetHelper._

/**
 * User: denis.bardadym
 * Date: 10/3/11
 * Time: 12:27 PM
 */

class SourceElementOps(se: SourceElementPage) {
  
  def renderBrancheSelector = w(se.repo){repo => 
    ".current_branch" #>
          SHtml.ajaxSelect(repo.git.branches.zip(repo.git.branches),
            if(repo.git.branches.contains(se.commit)) Full(se.commit) else Empty,
            value => S.redirectTo(repo.sourceTreeUrl(se.commit)+suffix(se.path)))
  }

  def renderBreadcrumbs = w(se.repo){repo => 
            ".breadcrumbs *" #> (a(repo.sourceTreeUrl(se.commit), Text(repo.name.get)) ++
              se.path.zipWithIndex.flatMap(a =>
                  <span class="slash"> / </span> ++ 
                  {if (se.path.size -1  != a._2) 
                    <a href={repo.sourceTreeUrl(se.commit) + se.path.dropRight(se.path.size - a._2 - 1).mkString("/","/", "")}>{a._1}</a> 
                  else 
                    <span>{a._1}</span>}))
          }
 
 def renderTree = w(se.repo){repo => {
   (if (se.path.isEmpty) ".parent" #> NodeSeq.Empty else ".parent *" #> (".name *" #> 
        <a href={repo.sourceTreeUrl(se.commit) + suffix(se.path.dropRight(1))}>..</a>)) &
   ".source_element *" #> repo.git.ls_tree(se.path, se.commit).map(s => {
        val c = repo.git.log(se.commit, suffix(se.path, "", "/") + s.path).next
        ".name *" #> (s match {
          case Tree(path, _) => <a href={repo.sourceTreeUrl(se.commit) + suffix(se.path) + "/" + path}>{path}/</a>
          case Blob(path, _) => <a href={repo.sourceBlobUrl(se.commit) + suffix(se.path) + "/" + path}>{path}</a>
        } ) &
        ".date *" #> (tryo {SnippetHelper.dateFormatter.format(c.getAuthorIdent.getWhen) }).openOr { "" }&
        ".last_commit *" #> (tryo { c.getShortMessage }).openOr { "" }
          
 })}}

 def renderBlob = w(se.repo){repo => 
    ".source_code" #> Text(repo.git.ls_cat(se.path.map(p => java.net.URLDecoder.decode(p, "UTF-8")), se.commit))
  }

}