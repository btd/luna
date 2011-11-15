package code.snippet

import net.liftweb._
import common.{Full, Loggable}
import util.Helpers._
import http._
import code.model.{UserDoc, SshKeyDoc}
import SnippetHelper._
import com.foursquare.rogue.Rogue._

/**
 * Created by IntelliJ IDEA.
 * User: den
 * Date: 24.09.11
 * Time: 17:34
 * To change this template use File | Settings | File Templates.
 */

class AuthOps extends Loggable with UserUI with SshKeyUI {
  private var email = ""
  private var password = ""
  private var login = ""

  def renderNewUserForm = {
    val newUser = UserDoc.createRecord
    val newKey = SshKeyDoc.createRecord.ownerId(newUser.id.get)
    userForm(newUser) &
    sshKeyForm(newKey) &
    button("Register", { 
      saveUser(newUser)
      saveSshKey(newKey)
    })
  }
 

  
}