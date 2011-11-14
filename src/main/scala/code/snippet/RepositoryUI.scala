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
	protected var name = "" 

	def repositoryForm(defaultName: String, onSubmit: () => Unit): NodeSeq => NodeSeq = {
    "name=name" #> SHtml.text(defaultName, {
          value: String =>
            name = value.trim
            if (name.isEmpty) S.error("Name field is empty")
        },
        "placeholder" -> "Name", "class" -> "textfield large") &
          "button" #>
            SHtml.button("Update", onSubmit, "class" -> "button")
  	}
	
}