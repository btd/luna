package code.snippet

import net.liftweb.common.Full
import net.liftweb.util.Helpers._
import xml.{Text, NodeSeq}
import code.model.UserDoc

/**
 * Created by IntelliJ IDEA.
 * User: den
 * Date: 24.09.11
 * Time: 15:40
 * To change this template use File | Settings | File Templates.
 */

object MyMenu {
  def your = {
    "*" #> (UserDoc.currentUser match {
      case Full(u) => <a href={"/" + u.login.get}>Your page</a>
      case _ => Text("")
    })
  }

  def admin = {
    "*" #> (UserDoc.currentUser match {
      case Full(u) => <a href={"/admin/" + u.login.get}>Admin</a>
      case _ => Text("")
    })
  }

  def signIn = {
    "*" #> (UserDoc.currentUser match {
      case Full(u) => Text("")
      case _ => <a href="/user/m/signin">Sign In</a>
    })

  }
}