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
import http._
import js.jquery._
import JqJE._
import JqJsCmds._
import util.Helpers._
import common._
import util.PassThru
import com.foursquare.rogue.Rogue._
import xml.{NodeSeq, Text}
import code.model._
import org.bson.types.ObjectId

trait RepositoryUI {
	private var name = "" 
  private var open_? = false


	def repositoryForm(repo: RepositoryDoc, buttonText:String, onSubmit: () => Any): NodeSeq => NodeSeq = {
    "name=name" #> SHtml.text(repo.name.get, v => name = v.trim, "placeholder" -> "Name", "class" -> "textfield large") &
    "name=open" #> SHtml.checkbox(repo.open_?.get, open_? = _) &
      "button" #> SHtml.button(buttonText, onSubmit, "class" -> "button")
    }

    def saveRepo(repo: RepositoryDoc)(): Any = {
      saveRepo(repo, r => r)
    }


    def saveRepo(repo: RepositoryDoc, postUpdate: (RepositoryDoc) => Any)(): Any = {
      val record = repo.name(name).open_?(open_?)
      record.validate match {
        case Nil => record.save
        case l => l.foreach(fe => S.error("repo", fe.msg))
      }
      postUpdate(record)
    }

    def updateRepo(repo: RepositoryDoc, postUpdate: (RepositoryDoc) => Any)(): Any = {
      if(repo.name.get != name) repo.name(name)
      if(repo.open_?.get != open_?) repo.open_?(open_?)
      
      repo.fields.filter(_.dirty_?).flatMap(_.validate) match {
        case Nil => { repo.update; postUpdate(repo) }
        case l => l.foreach(fe => S.error("repo", fe.msg))
      }
      
    }

    def deleteRepo(repo: RepositoryDoc, postDelete: (RepositoryDoc) => Any)() = { 

      repo.deleteDependend

      repo.delete_!

      postDelete(repo)
    }

  }