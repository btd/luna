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
import Utility._
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
          case t @ Tree(_) => <a href={repo.sourceTreeUrl(se.commit) + "/" + t.path}>{t.basename}/</a>
          case b @ Blob(_, _) => <a href={repo.sourceBlobUrl(se.commit) + "/" + b.path}>{b.basename}</a>
        } ) &
        ".date *" #> (tryo {escape(SnippetHelper.dateFormatter.format(c.getAuthorIdent.getWhen)) }).openOr { "" }&
        ".last_commit *" #> (tryo { escape(c.getShortMessage) }).openOr { "" }
          
 })}}

 def renderBlob = w(se.elem){_ match {
      case b @ Blob(_,_) => {
       if(b.viewable_? && !b.generated_? && !b.vendored_?) ".source_code" #> escape(b.data)
       else if(b.generated_?) ".source_code" #> "File is generated and not will be shown"
       else if(b.vendored_?) ".source_code" #> "Seems that no need to show this file"
       else if(b.image_?) ".source_code" #> "This is image"
       else if(b.binary_?) ".source_code" #> "This is binary file"
       else ".source_code" #> NodeSeq.Empty
      }
      case _ => ".source_code" #> NodeSeq.Empty
    }
    
  }

}