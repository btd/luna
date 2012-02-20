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

	def keysTable(keys: Seq[SshKeyBase[_]]): NodeSeq => NodeSeq = 
		".key" #> keys.map(key => {
			".key [id]" #> key.id.get.toString &
			".key_name *" #> (key.algorithm + " " + key.encodedKey.substring(0, 10) + "... " + key.comment) &
			".key_delete *" #> SHtml.a(Text("X")) {
                									key.delete_!
                									JqId(key.id.get.toString) ~> JqRemove()}

          })
	
	def sshKeyForm(key: SshKeyBase[_]): CssSel = {
    "name=ssh_key" #>
      SHtml.textarea(key.rawValue.get, v => ssh_key = v.replaceAll("""^\s+""", ""), "placeholder" -> "Enter your ssh key",
      "class" -> "textfield",
      "cols" -> "40", "rows" -> "20")      
  	}

  def sshKeyForm(key: SshKeyBase[_], buttonText: String, onSubmit: () => Any): NodeSeq => NodeSeq = 
    sshKeyForm(key) & button(buttonText, onSubmit)

  def fillKey(key: SshKeyBase[_]) = key.rawValue(ssh_key)

  def saveSshKey(key: SshKeyBase[_])() = {
    val record = key.rawValue(ssh_key).asInstanceOf[SshKeyBase[_]]
    //logger.debug(record)
    record.validate match {
      case Nil => record.save
      case l => l.foreach(fe => S.error("keys", fe.msg))
    }
  }

}