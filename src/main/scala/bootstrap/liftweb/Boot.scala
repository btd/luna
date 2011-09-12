package bootstrap.liftweb

import net.liftweb._
import common._
import db._
import http._
import sitemap._
import Loc._
import sshd.SshDaemon
import javax.naming.InitialContext
import javax.sql.DataSource
import actors.Actor
import entity.DefaultConnectionManager


/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot extends Loggable {
  def boot {
    DefaultConnectionIdentifier.jndiName = "jdbc/db"
    logger.warn("JNDI connection available (manyally) ? " + (((new InitialContext).lookup("java:/comp/env/jdbc/db").asInstanceOf[DataSource]) != null))
    logger.warn("JNDI connection available (lift) ? " + DB.jndiJdbcConnAvailable_?)

    if (!DB.jndiJdbcConnAvailable_?) {
      DB.defineConnectionManager(DefaultConnectionIdentifier, DefaultConnectionManager)
      LiftRules.unloadHooks.append(DefaultConnectionManager.close _)
    }

    new Actor {
      def act() {
        SshDaemon.start()
      }
    }.start()

    ResourceServer.allow {
      case "css" :: _ => true
    }

    // where to search snippet
    LiftRules.addToPackages("code")

    // Build SiteMap
    val entries = List(
      Menu.i("Home") / "index", // the simple way to declare a menu

      // more complex because this menu allows anything in the
      // /static path to be visible
      Menu(Loc("Static", Link(List("static"), true, "/static/index"),
        "Static Content")))

    // set the sitemap.  Note if you don't want access control for
    // each page, just comment this line out.
    LiftRules.setSiteMap(SiteMap(entries: _*))

    //Show the spinny image when an Ajax call starts
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)

    // Make the spinny image go away when it ends
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    // Use jQuery 1.4
    LiftRules.jsArtifacts = net.liftweb.http.js.jquery.JQuery14Artifacts

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    LiftRules.htmlProperties.default.set((r: Req) => new Html5Properties(r.userAgent))

  }
}
