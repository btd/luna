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

/**
 * User: denis.bardadym
 * Date: 10/3/11
 * Time: 12:27 PM
 */

class SourceElementOps(se: SourceElementPage) extends Loggable {
  
  def renderBrancheSelector = w(se.repo){repo => 
    ".current_branch" #>
          SHtml.ajaxSelect(repo.git.branches.zip(repo.git.branches),
            if(repo.git.branches.contains(se.commit)) Full(se.commit) else Empty,
            value => S.redirectTo(Sitemap.treeAtCommit.calcHref(se.copy(commit = value))))
  }

  def renderBreadcrumbs = w(se.repo){repo => 
            ".breadcrumbs *" #> (a(Sitemap.treeAtCommit.calcHref(se.copy(path = Nil)), Text(repo.name.get)) ++
              se.path.zipWithIndex.flatMap(a =>
                  <span class="slash"> / </span> ++ 
                  {if (se.path.size -1  != a._2) 
                    <a href={Sitemap.treeAtCommit.calcHref(se.copy(path = se.path.dropRight(se.path.size - a._2 - 1)))}>{a._1}</a> 
                  else 
                    <span>{a._1}</span>}))
          }
 
 def renderTree = w(se.repo){repo => {
   // NotifySubscriptionDoc.createRecord.who(repo.owner.id.get).repo(repo.id.get).onWhat(NotifyEvents.Push).output(NotifyOptions(Full(Email(repo.owner.email.get :: Nil)))).save
   (if (se.path.isEmpty) ".parent" #> NodeSeq.Empty else ".parent *" #> (".name *" #> 
        <a href={Sitemap.treeAtCommit.calcHref(se.copy(path = se.path.dropRight(1)))}>..</a>)) &
    tryo(repo.git.ls_tree(se.path, se.commit)).map(sourceList => 
     ".source_element *" #> sourceList.map(s => {
          val c = tryo { repo.git.log(se.commit, s.path).next } 
          ".name *" #> {
          s match {
            case t @ Tree(_) => <a href={Sitemap.treeAtCommit.calcHref(se.copy(path = t.path.split("/").toList))}>{t.basename}/</a>
            case b @ Blob(_, _) => <a href={Sitemap.blobAtCommit.calcHref(se.copy(path = b.path.split("/").toList))}>{b.basename}</a>
          }} &
          ".date *" #> c.map(cc => escape(dateFormat(cc.getCommitterIdent.getWhen))) &
          ".last_commit *" #> c.map(cc => escape(cc.getShortMessage) )
        })
     ).openOr(".source_element" #> NodeSeq.Empty)
      
 }}

 def renderBlob = w(se.elem){_ match {
      case b @ Blob(_,_) => {
        val rawHref = "/" + se.repo.get.owner.login.get + "/" + se.repo.get.name.get + "/raw/" + se.commit + "/" + b.path
       ".blob_header *" #> (
        ".history" #> a(Sitemap.historyAtCommit.calcHref(se), Text("history")) &
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
    
  }

}