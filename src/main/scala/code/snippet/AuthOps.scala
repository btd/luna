package code.snippet

import net.liftweb._
import common.{Full, Loggable}
import util.Helpers._
import http._
import code.model._
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
    val newKey = SshKeyUserDoc.createRecord.ownerId(newUser.id.get)

    userForm(newUser) &
    sshKeyForm(newKey) &
    button("Register", () => {
      fillUser(newUser)
      fillKey(newKey)
      newUser.validate match {
        case Nil => newKey.validate match {
            case Nil => {
              newUser.save
              newKey.save
              logger.debug(newUser)
              UserDoc.logUserIn(newUser, ()=> S.redirectTo(newUser.homePageUrl))
            }
            case l => l.foreach(fe => S.error(fe.msg))
          }
        case l => l.foreach(fe => S.error(fe.msg))
      }
    })
  }
 

  
}