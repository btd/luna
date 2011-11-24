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