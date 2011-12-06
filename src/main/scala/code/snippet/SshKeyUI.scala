package code.snippet

import bootstrap.liftweb._
import net.liftweb._
import http._
import util._
import js.jquery._
import JqJE._
import JqJsCmds._
import util.Helpers._
import common._
import util.PassThru
import com.foursquare.rogue.Rogue._
import xml.{NodeSeq, Text}
import code.model._
import SnippetHelper._
import org.bson.types.ObjectId

trait SshKeyUI extends Loggable {
	protected var ssh_key = "" 

	def keysTable(keys: Seq[SshKeyDoc]): NodeSeq => NodeSeq = 
		".keys" #> keys.map(key => {
			".key [id]" #> key.id.get.toString &
			".key *" #> (".key_name *" #> (key.algorithm + " " + key.encodedKey.substring(0, 10) + "... " + key.comment) &
						".key_delete *" #> SHtml.a(Text("X")) {
                									key.delete_!
                									JqId(key.id.get.toString) ~> JqRemove()})

          })
	
	def sshKeyForm(key: SshKeyDoc): CssSel = {
    "name=ssh_key" #>
      SHtml.textarea(key.rawValue.get, v => ssh_key = v.replaceAll("""^\s+""", ""), "placeholder" -> "Enter your ssh key",
      "class" -> "textfield",
      "cols" -> "40", "rows" -> "20")      
  	}

  def sshKeyForm(key: SshKeyDoc, buttonText: String, onSubmit: () => Any): NodeSeq => NodeSeq = 
    sshKeyForm(key) & button(buttonText, onSubmit)

  def fillKey(key: SshKeyDoc) = key.rawValue(ssh_key)

  def saveSshKey(key: SshKeyDoc)() = {
    val record = key.rawValue(ssh_key)
    logger.debug(record)
    record.validate match {
      case Nil => record.save
      case l => l.foreach(fe => S.error("keys", fe.msg))
    }
  }

}