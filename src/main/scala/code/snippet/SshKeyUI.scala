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

trait SshKeyUI {
	protected var ssh_key = "" 

	def keysTable(keys: Seq[SshKeyDoc]): NodeSeq => NodeSeq = 
		".keys" #> keys.map(key => {
			".key [id]" #> key.id.get.toString &
			".key *" #> (".key_name *" #> key.comment &
						".key_delete *" #> SHtml.a(Text("X")) {
                									key.delete_!
                									JqId(key.id.get.toString) ~> JqRemove()})

          })
	
	def sshKeyForm(onSubmit: () => Unit): NodeSeq => NodeSeq = {
    "name=ssh_key" #>
      SHtml.textarea(ssh_key, {
        value: String =>
          ssh_key = value.replaceAll("""^\s+""", "")
          if (ssh_key.isEmpty) S.error("keys", "Ssh Key are empty")
      }, "placeholder" -> "Enter your ssh key",
      "class" -> "textfield",
      "cols" -> "40", "rows" -> "20") &
      "button" #> SHtml.button("Add key", onSubmit, "class" -> "button")
  	}
	
}