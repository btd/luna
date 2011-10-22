package bootstrap.liftweb

import net.liftweb._
import common._
import http._
import mongodb.{DefaultMongoIdentifier, MongoDB}
import sitemap._
import Loc._
import util.Helpers._
import sitemap.LocPath._
import sshd.SshDaemon
import actors.Actor
import xml.{NodeSeq, Text}
import sshd.git.GitDaemon
import com.mongodb.Mongo
import code.model.UserDoc

trait WithUser {
  def userName: String

  lazy val user = UserDoc.find("login", userName)
}

case class UserPage(userName: String) extends WithUser { }

trait WithRepo extends WithUser {
  def repoName : String

  lazy val repo = user match {
    case Full(u) => tryo {
      u.repos.filter(_.name.get == repoName).head
    } or {
      Empty
    }
    case _ => Empty
  }
}

case class RepoPage(userName: String, repoName: String) extends WithRepo {}

case class RepoAtCommitPage(userName: String, repoName: String, commit: String) extends WithRepo {}

case class SourceElementPage(userName: String, repoName: String, commit: String, path: List[String]) extends WithRepo {}


// TODO FIXME
object ValidUser {
  def unapply(login: String): Option[String] = Full(login)

  //User.withLogin(login) match {
  //  case Full(u) => Full(login)
  //  case _ => None
  //}
}

// TODO FIXME
object ValidRepo {
  def unapply(repo: String): Option[String] = Full(repo)

  //User.withLogin(login) match {
  //  case Full(u) => Full(login)
  //  case _ => None
  //}
}

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot extends Loggable {
  def boot {
    MongoDB.defineDb(DefaultMongoIdentifier, new Mongo, "grt") //TODO както секюрно надо это делать

    new Actor {
      def act() {
        SshDaemon.start
      }
    }.start()

    new Actor {
      def act() {
        GitDaemon.start
      }
    }.start()

    ResourceServer.allow {
      case "css" :: _ => true
      case "js" :: _ => true
    }

    // where to search snippet
    LiftRules.addToPackages("code")

    LiftRules.explicitlyParsedSuffixes = Set()

    val indexPage = Menu.i("Home") / "index" >> If(() => !UserDoc.loggedIn_?, () => RedirectResponse(UserDoc.currentUser.get.homePageUrl))
    // val listPage = Menu.i("List") / "list"

    val userPage = Menu.param[UserPage]("userPage",
      new LinkText[UserPage](up => Text("User " + up.userName)),
      login => Full(UserPage(login)),
      up => up.userName) / "list" / * >> Template(() => Templates("list" :: Nil) openOr NodeSeq.Empty)

    val userAdminPage = Menu.param[UserPage]("userAdminPage",
      new LinkText[UserPage](up => Text("User " + up.userName)),
      login => Full(UserPage(login)),
      up => up.userName) / "admin" / * >> Template(() => Templates("admin" :: "adminUser" :: Nil) openOr NodeSeq.Empty)

    val userRepoAdminPage = Menu.params[RepoPage]("userRepoAdminPage",
      new LinkText[RepoPage](urp => Text("Repo " + urp.repoName)),
      list => list match {
        case login :: repo :: Nil => Full(RepoPage(login, repo))
        case _ => Empty
      },
      urp => urp.userName :: urp.repoName :: Nil) / "admin" / * / * >> Template(() => Templates("admin" :: "adminRepo" :: Nil) openOr NodeSeq.Empty)

    val blobPage = Menu.params[SourceElementPage]("blobPage",
      new LinkText[SourceElementPage](stp => Text("Repo " + stp.repoName)),
      list => {

        list match {
          case login :: repo :: commit :: path => Full(SourceElementPage(login, repo, commit, path))
          case _ => Empty
        }
      },
      stp => (stp.userName :: stp.repoName :: stp.commit :: Nil) ::: stp.path) / * / * / "blob" / * / ** >> Template(() => Templates("repo" :: "blob" :: Nil) openOr NodeSeq.Empty)

    val emptyRepoPage = Menu.params[SourceElementPage]("emptyRepoPage",
      new LinkText[SourceElementPage](stp => Text("Repo " + stp.repoName)),
      list => {

        list match {
          case login :: repo :: Nil => Full(SourceElementPage(login, repo, "", Nil))
          case _ => Empty
        }
      },
      stp => stp.userName :: stp.repoName :: Nil) / * / * / "tree" >>
      Template(() => Templates("repo" :: "default" :: Nil) openOr NodeSeq.Empty) >>
      TestValueAccess(sp =>
        sp match {
          case Full(ssp) => {
            ssp.repo match {
              case Full(repo) if (repo.git.inited_?) => Full(RedirectResponse(repo.sourceTreeUrl))
              case _ => Empty
            }
          }
          case _ => Empty
        })

    val sourceTreePage = Menu.params[SourceElementPage]("sourceTreePage",
      new LinkText[SourceElementPage](stp => Text("Repo " + stp.repoName)),
      list => {

        list match {
          case login :: repo :: commit :: path => Full(SourceElementPage(login, repo, commit, path))
          case _ => Empty
        }
      },
      stp => (stp.userName :: stp.repoName :: stp.commit :: Nil) ::: stp.path) / * / * / "tree" / * / ** >>
      ValueTemplate(sp =>
        sp match {
          case Full(ssp) => {
            ssp.repo match {
              case Full(repo) if (repo.git.inited_?) => Templates("repo" :: "tree" :: Nil) openOr NodeSeq.Empty
              case Full(repo) => Templates("repo" :: "default" :: Nil) openOr NodeSeq.Empty
              case _ => NodeSeq.Empty
            }
          }
          case _ => NodeSeq.Empty
        })

    val emptyCommitsPage = Menu.params[RepoPage]("emptyCommitsPage",
      new LinkText[RepoPage](urp => Text("Repo " + urp.repoName)),
      list => list match {
        case login :: repo ::   Nil => Full(RepoPage(login, repo))
        case _ => Empty
      },
      urp => urp.userName :: urp.repoName :: Nil) / * / * / "commits" >>
      Template(() => Templates("repo" :: "commit" :: "default" :: Nil) openOr NodeSeq.Empty) >>
      TestValueAccess(sp =>
        sp match {
          case Full(ssp) => {
            ssp.repo match {
              case Full(repo) if (repo.git.inited_?) => Full(RedirectResponse(repo.commitsUrl))
              case _ => Empty
            }
          }
          case _ => Empty
        })



    val allCommitsPage = Menu.params[RepoAtCommitPage]("allCommitsPage",
      new LinkText[RepoAtCommitPage](urp => Text("Repo " + urp.repoName)),
      list => list match {
        case login :: repo :: commit :: Nil => Full(RepoAtCommitPage(login, repo, commit))
        case _ => Empty
      },
      urp => urp.userName :: urp.repoName :: urp.commit:: Nil) / * / * / "commits" / * >> Template(() => Templates("repo" :: "commit" :: "all" :: Nil) openOr NodeSeq.Empty)

    val commitPage = Menu.params[RepoAtCommitPage]("commitPage",
         new LinkText[RepoAtCommitPage](urp => Text("Repo " + urp.repoName)),
         list => list match {
           case login :: repo :: commit :: Nil => Full(RepoAtCommitPage(login, repo, commit))
           case _ => Empty
         },
         urp => urp.userName :: urp.repoName :: urp.commit:: Nil) / * / * / "commit" / * >> Template(() => Templates("repo" :: "commit" :: "one" :: Nil) openOr NodeSeq.Empty)

    val newPullRequestPage = Menu.params[RepoPage]("newPullRequestPage",
      new LinkText[RepoPage](urp => Text("Repo " + urp.repoName)),
      list => list match {
        case login :: repo ::   Nil => Full(RepoPage(login, repo))
        case _ => Empty
      },
      urp => urp.userName :: urp.repoName :: Nil) / * / * / "pull-request" >>
      Template(() => Templates("repo" :: "pull-request" :: "new" :: Nil) openOr NodeSeq.Empty)

    val signInPage = Menu.i("Sign In") / "user" / "m" / "signin"

    val loginPage = Menu.i("Log In") / "user" / "m" / "login"

    val newUserPage = Menu.i("Registration") / "user" / "m" / "new"

    // Build SiteMap
    val entries = List(
      indexPage,
      userPage,
      signInPage,
      loginPage,
      newUserPage,
      userAdminPage,
      userRepoAdminPage,
      sourceTreePage, blobPage, emptyRepoPage, emptyCommitsPage, allCommitsPage, commitPage, newPullRequestPage)
    //Menu.i("Home") / "index", // the simple way to declare a menu
    //Menu.i("New User") / "new",

    //)
    LiftRules.ajaxRetryCount = Full(1)

    LiftRules.statelessRewrite.append {
      case RewriteRequest(ParsePath("index" :: Nil, _, _, true), _, _) =>

        RewriteResponse("index" :: Nil, true)
      case RewriteRequest(ParsePath(ValidUser(user) :: Nil, _, _, false), _, _) =>

        RewriteResponse("list" :: user :: Nil, Map[String, String]())

    }



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
