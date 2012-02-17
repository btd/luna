package bootstrap.liftweb

import net.liftweb._
import common._
import util._
import http._
import mongodb.{DefaultMongoIdentifier, MongoDB}
import http.auth._
import sitemap._
import Loc._
import util.Helpers._
import sitemap.LocPath._
import daemon.sshd.SshDaemon
import actors.Actor
import xml.{NodeSeq, Text}
import daemon.git.GitDaemon
import com.mongodb.Mongo
import code.model._

trait WithUser {
  def userName: String

  lazy val user = UserDoc.find("login", userName)
}

case class UserPage(userName: String) extends WithUser

trait WithRepo extends WithUser {
  def repoName: String

  lazy val repo = user match {

    case Full(u) => (tryo {  u.repos.filter(_.name.get == repoName).head } or { Empty }) match {
      case Full(r) if (r.open_?.get) => Full(r)
      case Full(r) if (r.canPush_?(UserDoc.currentUser)) => Full(r)
      case _ => Empty
    }
        
    case _ => Empty
  }
}

trait WithPullRequest {
  def pullRequestId: String

  lazy val pullRequest = PullRequestDoc.find(pullRequestId)
}

trait WithCommit extends WithRepo {
  def commit: String
}

case class RepoPage(userName: String, repoName: String) extends WithRepo

case class RepoAtCommitPage(userName: String, repoName: String, commit: String) extends WithCommit

case class PullRequestRepoPage(userName: String, repoName: String, pullRequestId: String)  extends WithPullRequest with WithRepo

case class SourceElementPage(userName: String, repoName: String, commit: String, path: List[String]) extends WithCommit {
  
  private lazy val reversedPath = path.reverse

  lazy val elem = repo.flatMap(r => tryo { r.git.ls_tree(reversedPath.tail.reverse, commit).filter(_.basename == reversedPath.head).head } or {Empty})
}




/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot extends Loggable {
  def boot {
    val dbHost = Props.get("db.host", "localhost")
    val dbPort = Props.getInt("db.port", 27017)
    val dbName = Props.get("db.name", "grt")

    Props.get("db.user") match {
      case Full(userName) => {
        Props.requireOrDie("db.password")
        MongoDB.defineDbAuth(DefaultMongoIdentifier, new Mongo(dbHost, dbPort), dbName, userName, Props.get("db.password").get)
      }
      case _ => 
        MongoDB.defineDb(DefaultMongoIdentifier, new Mongo(dbHost, dbPort), dbName)
    }

    tryo(SshDaemon.init)
    tryo(GitDaemon.init)

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
      up => up.userName) / "list" / * >>
      ValueTemplate(upBox => upBox.flatMap(up => up.user).flatMap(u => Templates("list" :: Nil))
          .openOr(Templates("404" :: Nil).openOr(NodeSeq.Empty)))

    val userAdminPage = Menu.param[UserPage]("userAdminPage",
      new LinkText[UserPage](up => Text("User " + up.userName)),
      login => Full(UserPage(login)),
      up => up.userName) / "admin" / *  >>
      ValueTemplate(upBox => upBox.flatMap(up => up.user)
        .filter(_.is(UserDoc.currentUser))
        .flatMap(u => Templates("admin" :: "adminUser" :: Nil))
          .openOr(Templates("404" :: Nil).openOr(NodeSeq.Empty)))
     

    val userRepoAdminPage = Menu.params[RepoPage]("userRepoAdminPage",
      new LinkText[RepoPage](urp => Text("Repo " + urp.repoName)),
      list => list match {
        case login :: repo :: Nil => Full(RepoPage(login, repo))
        case _ => Empty
      },
      urp => urp.userName :: urp.repoName :: Nil) / "admin" / * / * >>
      ValueTemplate(upBox => upBox.flatMap(up => up.user)
        .filter(_.is(UserDoc.currentUser))
        .flatMap(u => Templates("admin" :: "adminRepo" :: Nil))
          .openOr(Templates("404" :: Nil).openOr(NodeSeq.Empty)))

    val blobPage = Menu.params[SourceElementPage]("blobPage",
      new LinkText[SourceElementPage](stp => Text("Repo " + stp.repoName)),
      list => list match {
          case login :: repo :: commit :: path => Full(SourceElementPage(login, repo, commit, path))
          case _ => Empty
      },
      stp => (stp.userName :: stp.repoName :: stp.commit :: Nil) ::: stp.path) / * / * / "blob" / * / **  >>
      ValueTemplate(upBox =>
        upBox.flatMap(rp => rp.repo).filter(r => r.canPull_?(UserDoc.currentUser))
          .flatMap(r => Templates("repo" :: "blob" :: Nil))
          .openOr(Templates("404" :: Nil).openOr(NodeSeq.Empty)))
      

    val emptyRepoPage = Menu.params[SourceElementPage]("emptyRepoPage",
      new LinkText[SourceElementPage](stp => Text("Repo " + stp.repoName)),
      list => {

        list match {
          case login :: repo :: Nil => Full(SourceElementPage(login, repo, "", Nil))
          case _ => Empty
        }
      },
      stp => stp.userName :: stp.repoName :: Nil) / * / * / "tree" >>
      ValueTemplate(spBox =>
        spBox.flatMap(rp => rp.repo).filter(r => r.canPull_?(UserDoc.currentUser))
          .flatMap(r => Templates("repo" :: "tree" :: Nil))
          .openOr(Templates("404" :: Nil).openOr(NodeSeq.Empty))) >>
      TestValueAccess(_.flatMap(rp => rp.repo).filter(r => r.git.inited_?)
          .flatMap(r => Full(RedirectResponse(r.sourceTreeUrl))))

    val sourceTreePage = Menu.params[SourceElementPage]("sourceTreePage",
      new LinkText[SourceElementPage](stp => Text("Repo " + stp.repoName)),
      list => {
        list match {
          case login :: repo :: commit :: path => Full(SourceElementPage(login, repo, commit, path))
          case _ => Empty
        }
      },
      stp => (stp.userName :: stp.repoName :: stp.commit :: Nil) ::: stp.path) / * / * / "tree" / * / ** >>
      ValueTemplate(_.flatMap(rp => rp.repo).filter(r => r.canPull_?(UserDoc.currentUser))
          .flatMap{
            case r if r.git.inited_? => Templates("repo" :: "tree" :: Nil)
            case _ => Templates("repo" :: "default" :: Nil)
          }.openOr(Templates("404" :: Nil).openOr(NodeSeq.Empty)))

    val emptyCommitsPage = Menu.params[RepoPage]("emptyCommitsPage",
      new LinkText[RepoPage](urp => Text("Repo " + urp.repoName)),
      list => list match {
        case login :: repo :: Nil => Full(RepoPage(login, repo))
        case _ => Empty
      },
      urp => urp.userName :: urp.repoName :: Nil) / * / * / "commits" >>
      ValueTemplate(spBox =>
        spBox.flatMap(rp => rp.repo).filter(r => r.canPull_?(UserDoc.currentUser))
          .flatMap(r => Templates("repo" :: "commit" :: "default" :: Nil))
          .openOr(Templates("404" :: Nil).openOr(NodeSeq.Empty))) >>
      TestValueAccess(_.flatMap(rp => rp.repo).filter(r => r.git.inited_?)
          .flatMap(r => Full(RedirectResponse(r.commitsUrl))))


    val allCommitsPage = Menu.params[RepoAtCommitPage]("allCommitsPage",
      new LinkText[RepoAtCommitPage](urp => Text("Repo " + urp.repoName)),
      list => list match {
        case login :: repo :: commit :: Nil => Full(RepoAtCommitPage(login, repo, commit))
        case _ => Empty
      },
      urp => urp.userName :: urp.repoName :: urp.commit :: Nil) / * / * / "commits" / * >>
      ValueTemplate(_.flatMap(rp => rp.repo).filter(r => r.canPull_?(UserDoc.currentUser))
          .flatMap{
            case r if r.git.inited_? => Templates("repo" :: "commit" :: "all" :: Nil)
            case _ => Templates("repo" :: "commit" :: "default" :: Nil)
          }.openOr(Templates("404" :: Nil).openOr(NodeSeq.Empty)))

    val commitPage = Menu.params[RepoAtCommitPage]("commitPage",
      new LinkText[RepoAtCommitPage](urp => Text("Repo " + urp.repoName)),
      list => list match {
        case login :: repo :: commit :: Nil => Full(RepoAtCommitPage(login, repo, commit))
        case _ => Empty
      },
      urp => urp.userName :: urp.repoName :: urp.commit :: Nil) / * / * / "commit" / * >>
      ValueTemplate(_.flatMap(rp => rp.repo).filter(r => r.canPull_?(UserDoc.currentUser))
          .flatMap(r => Templates("repo" :: "commit" :: "one" :: Nil))
          .openOr(Templates("404" :: Nil).openOr(NodeSeq.Empty)))

    val newPullRequestPage = Menu.params[RepoPage]("newPullRequestPage",
      new LinkText[RepoPage](urp => Text("Repo " + urp.repoName)),
      list => list match {
        case login :: repo :: Nil => Full(RepoPage(login, repo))
        case _ => Empty
      },
      urp => urp.userName :: urp.repoName :: Nil) / * / * / "pull-requests" / "new" >>
      ValueTemplate(_.flatMap(rp => rp.repo).filter(r => r.canPush_?(UserDoc.currentUser))
          .flatMap(r => Templates("repo" :: "pull-request" :: "new" :: Nil))
          .openOr(Templates("404" :: Nil).openOr(NodeSeq.Empty)))

    val allPullRequestPage = Menu.params[RepoPage]("allPullRequestPage",
      new LinkText[RepoPage](urp => Text("Repo " + urp.repoName)),
      list => list match {
        case login :: repo :: Nil => Full(RepoPage(login, repo))
        case _ => Empty
      },
      urp => urp.userName :: urp.repoName :: Nil) / * / * / "pull-requests" >>
      ValueTemplate(_.flatMap(rp => rp.repo).filter(r => r.canPull_?(UserDoc.currentUser))
          .flatMap(r => Templates("repo" :: "pull-request" :: "all" :: Nil))
          .openOr(Templates("404" :: Nil).openOr(NodeSeq.Empty)))

    val onePullRequestPage = Menu.params[PullRequestRepoPage]("onePullRequestPage",
      new LinkText[PullRequestRepoPage](urp => Text("Repo " + urp.repoName)),
      list => list match {
        case login :: repo :: pullRequestId :: Nil => Full(PullRequestRepoPage(login, repo, pullRequestId))
        case _ => Empty
      },
      urp => urp.userName :: urp.repoName :: urp.pullRequestId :: Nil) / * / * / "pull-request" / * >>
      ValueTemplate(_.flatMap(rp => rp.repo).filter(r => r.canPull_?(UserDoc.currentUser))
          .flatMap(r => Templates("repo" :: "pull-request" :: "one" :: Nil))
          .openOr(Templates("404" :: Nil).openOr(NodeSeq.Empty)))

    val signInPage = Menu.i("Sign In") / "user" / "m" / "signin" >> If(() => !UserDoc.loggedIn_?, () => RedirectResponse(UserDoc.currentUser.get.homePageUrl))

    val loginPage = Menu.i("Log In") / "user" / "m" / "login"

    val newUserPage = Menu.i("Registration") / "user" / "m" / "new"

    val notifyPushPage = Menu.params[RepoPage]("notifyPushPage",
      new LinkText[RepoPage](urp => Text("Repo " + urp.repoName)),
      list => list match {
        case login :: repo :: Nil => Full(RepoPage(login, repo))
        case _ => Empty
      },
      urp => urp.userName :: urp.repoName :: Nil) / * / * / "notify" >>
      ValueTemplate(rpBox =>
        rpBox.flatMap(rp => rp.repo).filter(r => UserDoc.loggedIn_?)
          .flatMap(r => Templates("notification" :: "push" :: Nil))
          .openOr(Templates("404" :: Nil).openOr(NodeSeq.Empty))
          
      )

    // Build SiteMap
    val entries = List(
      indexPage,
      userPage,
      signInPage,
      loginPage,
      newUserPage,
      userAdminPage,
      userRepoAdminPage,
      sourceTreePage,
      blobPage,
      emptyRepoPage,
      emptyCommitsPage,
      allCommitsPage,
      commitPage,
      newPullRequestPage,
      allPullRequestPage,
      onePullRequestPage,
      notifyPushPage)
    
    LiftRules.setSiteMap(SiteMap(entries: _*))

    LiftRules.dispatch.append(code.snippet.RawFileStreamingSnippet)
    LiftRules.dispatch.append(code.snippet.GitHttpSnippet)

    LiftRules.ajaxRetryCount = Full(1)
    LiftRules.ajaxPostTimeout = 15000

    LiftRules.statelessRewrite.append {
      case RewriteRequest(ParsePath("index" :: Nil, _, _, true), _, _) =>

        RewriteResponse("user" :: "m" :: "signin" :: Nil, true)

      case RewriteRequest(ParsePath(user :: Nil, _, _, false), _, _) =>

        RewriteResponse("list" :: user :: Nil, Map[String, String]())

    }

    def open_?(userName: String, repoName: String):Boolean = {
      RepositoryDoc.byUserLoginAndRepoName(userName, repoName.substring(0, repoName.length - 4)) match {
        case Some(r) => r.open_?.get
        case _ => false
      }
    }

    LiftRules.httpAuthProtectedResource.prepend{ 
      case Req(userName :: repoName :: "info" :: "refs" :: Nil, _, _) 
        if(repoName.endsWith(".git") && 
          !S.param("service").isEmpty && (
              S.param("service").get == "git-receive-pack" || 
              !open_?(userName, repoName))) => Empty
      case Req(userName :: repoName :: "git-receive-pack" :: Nil, _, PostRequest) 
        if(repoName.endsWith(".git")) => Empty
      case Req(userName :: repoName :: "git-upload-pack" :: Nil, _, PostRequest) 
        if(repoName.endsWith(".git") && !open_?(userName, repoName)) => Empty 
    } 

    LiftRules.authentication = HttpBasicAuthentication("lift") { 
      case (username, password, Req(userName :: repoName :: _, _, _)) if repoName.endsWith(".git") => { 
        UserDoc.byName(username) match { 
          case Some(user) if user.password.match_?(password) => 
            RepositoryDoc.byUserLoginAndRepoName(userName, repoName.substring(0, repoName.length - 4)) match {
              case Some(r) => r.canPush_?(Some(user))
              case _ => false
            }
          case _ => false 
        } 
      } 
    } 

    // Use jQuery 1.4
    LiftRules.jsArtifacts = net.liftweb.http.js.jquery.JQuery14Artifacts

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    LiftRules.htmlProperties.default.set((r: Req) => new Html5Properties(r.userAgent))

  }

}
