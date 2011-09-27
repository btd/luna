package code.snippet

import entity.User
import net.liftweb.common.Full
import net.liftweb.util.Helpers._
import xml.{Text, NodeSeq}

/**
 * Created by IntelliJ IDEA.
 * User: den
 * Date: 24.09.11
 * Time: 15:40
 * To change this template use File | Settings | File Templates.
 */

object MyMenu {
  def your = {
    "*" #> (User.currentUserId match {
      case Full(u) => <a href={"/" + u}>Your page</a>
      case _ => Text("")
    })
  }

  def admin = {
    "*" #> (User.currentUserId match {
      case Full(u) => <a href={"/admin/" + u}>Admin</a>
      case _ => Text("")
    })
  }

  def signIn = {
    "*" #> (User.currentUserId match {
      case Full(u) => Text("")
      case _ => <a href="/user/signin">Sign In</a>
    })

  }
}