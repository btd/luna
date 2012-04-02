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
import code.lib._
import util._
import xml._
import Utility._
import SnippetHelper._
import Sitemap._

/**
 * User: denis.bardadym
 * Date: 10/3/11
 * Time: 12:27 PM
 */

class SourceElementOps(se: SourceElement) extends Loggable {
  
  def renderBrancheSelector = 
    ".current_branch" #>
          SHtml.ajaxSelect(se.repo.git.branches.zip(se.repo.git.branches),
            if(se.repo.git.branches.contains(se.commit)) Full(se.commit) else Empty,
            value => S.redirectTo(treeAtCommit.calcHref(Tree(se.repo, value, se.pathLst))))


  def renderBreadcrumbs = 
    ".breadcrumbs *" #> (a(treeAtCommit.calcHref(Tree(se.repo, se.commit, Nil)), Text(se.repo.name.get)) ++
      se.pathLst.zipWithIndex.flatMap(a =>
          <span class="slash"> / </span> ++ 
          {if (se.pathLst.size -1  != a._2) 
            <a href={treeAtCommit.calcHref(Tree(se.repo, se.commit, se.pathLst.dropRight(se.pathLst.size - a._2 - 1)))}>{a._1}</a> 
          else 
            <span>{a._1}</span>}))
          
 
 def renderTree = 
   (if (se.pathLst.isEmpty) ".parent" #> NodeSeq.Empty else ".parent *" #> (".name *" #> 
        <a href={treeAtCommit.calcHref(Tree(se.repo, se.commit, se.pathLst.dropRight(1)))}>..</a>)) &
   (se match {
      case t @ Tree(_, _, _) => {
        t.data.map(sourceList => 
         ".source_element *" #> sourceList.map(s => {
              val c = tryo { se.repo.git.log(se.commit, s.pathLst).next } 
              ".name *" #> {
                  s match {
                    case t @ Tree(_, _, _) => <a href={treeAtCommit.calcHref(t)}>{t.name}/</a>
                    case b @ Blob(_, _, _, _) => <a href={blobAtCommit.calcHref(b)}>{b.name}</a>
                  }} &
              ".date *" #> c.map(cc => escape(dateFormat(cc.getCommitterIdent.getWhen))) &
              ".last_commit *" #> c.map(cc => escape(cc.getShortMessage) )
         })).openOr(".source_element" #> NodeSeq.Empty)
         
      }
      case _ => ".source_element" #> NodeSeq.Empty
   })

 def renderBlob = se match {
    case b @ Blob(_,_, _, _) => {
      val rawHref = "/" + se.repo.owner.login.get + "/" + se.repo.name.get + "/raw/" + se.commit + "/" + b.path
     ".blob_header *" #> (
      ".history" #> a(historyAtCommit.calcHref(se), Text("history")) &
      ".raw" #> a(rawHref, Text("raw"))) &
     (if(b.viewable_? && !b.generated_? && !b.vendored_?) ".source_code" #> b.data
            else if(b.generated_?) ".source_code" #> "File is generated and not will be shown"
            else if(b.vendored_?) ".source_code" #> "Seems that no need to show this file"
            else if(b.image_?) ".blob *" #> <div class="img"><img src={rawHref}/></div>
            else if(b.binary_?) ".source_code" #> "This is binary file"
            else ".source_code" #> NodeSeq.Empty)
    }
    case _ => ".source_code" #> NodeSeq.Empty
  }
    
  def renderRepositoryBlockDefault = 
    renderRepositoryBlock(se.repo, 
                          se.repo.owner, 
                          r => <span><a href={userRepos.calcHref(r.owner)}>{r.owner.login.get}</a>/{r.name.get}</span>,
                          r => S.redirectTo(defaultTree.calcHref(r)),
                          r => S.redirectTo(defaultTree.calcHref(r)) ) 

}