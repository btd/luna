package bootstrap.liftweb

import net.liftweb._
import common._
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
import code.model.{PullRequestDoc, UserDoc}

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
    MongoDB.defineDb(DefaultMongoIdentifier, new Mongo, "grt") //TODO както секюрно надо это делать

    SshDaemon.init
    GitDaemon.init

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
      ValueTemplate(up =>  up match {
        case Full(up1) => up1.user match {
          case Full(user) =>  Templates("list" :: Nil) openOr NodeSeq.Empty
          case _ => Templates("404" :: Nil) openOr NodeSeq.Empty
        }
        case _ => Templates("404" :: Nil) openOr NodeSeq.Empty
      })

    val userAdminPage = Menu.param[UserPage]("userAdminPage",
      new LinkText[UserPage](up => Text("User " + up.userName)),
      login => Full(UserPage(login)),
      up => up.userName) / "admin" / *  >>
      ValueTemplate(up =>  up match {
        case Full(up1) => up1.user match {
          case Full(user) => {
            UserDoc.currentUser match {
              case Full(cuser) if (cuser.id.get == user.id.get) => Templates("admin" :: "adminUser" :: Nil) openOr NodeSeq.Empty
              case _ => Templates("404" :: Nil) openOr NodeSeq.Empty
            }
          }
          case _ => Templates("404" :: Nil) openOr NodeSeq.Empty
        }
        case _ => Templates("404" :: Nil) openOr NodeSeq.Empty
      })




    val userRepoAdminPage = Menu.params[RepoPage]("userRepoAdminPage",
      new LinkText[RepoPage](urp => Text("Repo " + urp.repoName)),
      list => list match {
        case login :: repo :: Nil => Full(RepoPage(login, repo))
        case _ => Empty
      },
      urp => urp.userName :: urp.repoName :: Nil) / "admin" / * / * >>
      ValueTemplate(up =>  up match {
        case Full(up1) => up1.repo match {
          case Full(repo) => {
            UserDoc.currentUser match {
              case Full(cuser) if (cuser.id.get == repo.owner.id.get) => Templates("admin" :: "adminRepo" :: Nil) openOr NodeSeq.Empty
              case _ => Templates("404" :: Nil) openOr NodeSeq.Empty
            }
          }
          case _ => Templates("404" :: Nil) openOr NodeSeq.Empty
        }
        case _ => Templates("404" :: Nil) openOr NodeSeq.Empty
      })

    val blobPage = Menu.params[SourceElementPage]("blobPage",
      new LinkText[SourceElementPage](stp => Text("Repo " + stp.repoName)),
      list => {

        list match {
          case login :: repo :: commit :: path => Full(SourceElementPage(login, repo, commit, path))
          case _ => Empty
        }
      },
      stp => (stp.userName :: stp.repoName :: stp.commit :: Nil) ::: stp.path) / * / * / "blob" / * / **  >>
      ValueTemplate(up =>  up match {
        case Full(up1) => up1.repo match {
          case Full(repo) =>  Templates("repo" :: "blob" :: Nil) openOr NodeSeq.Empty
          case _ => Templates("404" :: Nil) openOr NodeSeq.Empty
        }
        case _ => Templates("404" :: Nil) openOr NodeSeq.Empty
      })

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
        }) >> LocGroup("repo")

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
        case login :: repo :: Nil => Full(RepoPage(login, repo))
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
        }) >> LocGroup("repo")



    val allCommitsPage = Menu.params[RepoAtCommitPage]("allCommitsPage",
      new LinkText[RepoAtCommitPage](urp => Text("Repo " + urp.repoName)),
      list => list match {
        case login :: repo :: commit :: Nil => Full(RepoAtCommitPage(login, repo, commit))
        case _ => Empty
      },
      urp => urp.userName :: urp.repoName :: urp.commit :: Nil) / * / * / "commits" / *   >>
      ValueTemplate(up =>  up match {
        case Full(up1) => up1.repo match {
          case Full(repo) =>  Templates("repo" :: "commit" :: "all" :: Nil) openOr NodeSeq.Empty
          case _ => Templates("404" :: Nil) openOr NodeSeq.Empty
        }
        case _ => Templates("404" :: Nil) openOr NodeSeq.Empty
      })


    val commitPage = Menu.params[RepoAtCommitPage]("commitPage",
      new LinkText[RepoAtCommitPage](urp => Text("Repo " + urp.repoName)),
      list => list match {
        case login :: repo :: commit :: Nil => Full(RepoAtCommitPage(login, repo, commit))
        case _ => Empty
      },
      urp => urp.userName :: urp.repoName :: urp.commit :: Nil) / * / * / "commit" / * >>
      ValueTemplate(up =>  up match {
        case Full(up1) => up1.repo match {
          case Full(repo) =>  Templates("repo" :: "commit" :: "one" :: Nil) openOr NodeSeq.Empty
          case _ => Templates("404" :: Nil) openOr NodeSeq.Empty
        }
        case _ => Templates("404" :: Nil) openOr NodeSeq.Empty
      })

    val newPullRequestPage = Menu.params[RepoPage]("newPullRequestPage",
      new LinkText[RepoPage](urp => Text("Repo " + urp.repoName)),
      list => list match {
        case login :: repo :: Nil => Full(RepoPage(login, repo))
        case _ => Empty
      },
      urp => urp.userName :: urp.repoName :: Nil) / * / * / "pull-requests" / "new" >>
      ValueTemplate(rp =>  rp match {
        case Full(up1) => up1.repo match {
          case Full(repo) if(repo.canPush_?(UserDoc.currentUser)) => Templates("repo" :: "pull-request" :: "new" :: Nil) openOr NodeSeq.Empty
          case _ => Templates("404" :: Nil) openOr NodeSeq.Empty
        }
        case _ => Templates("404" :: Nil) openOr NodeSeq.Empty
      })

    val allPullRequestPage = Menu.params[RepoPage]("allPullRequestPage",
      new LinkText[RepoPage](urp => Text("Repo " + urp.repoName)),
      list => list match {
        case login :: repo :: Nil => Full(RepoPage(login, repo))
        case _ => Empty
      },
      urp => urp.userName :: urp.repoName :: Nil) / * / * / "pull-requests" >>
      ValueTemplate(up =>  up match {
        case Full(up1) => up1.repo match {
          case Full(repo) =>  Templates("repo" :: "pull-request" :: "all" :: Nil) openOr NodeSeq.Empty
          case _ => Templates("404" :: Nil) openOr NodeSeq.Empty
        }
        case _ => Templates("404" :: Nil) openOr NodeSeq.Empty
      })

    val onePullRequestPage = Menu.params[PullRequestRepoPage]("onePullRequestPage",
      new LinkText[PullRequestRepoPage](urp => Text("Repo " + urp.repoName)),
      list => list match {
        case login :: repo :: pullRequestId :: Nil => Full(PullRequestRepoPage(login, repo, pullRequestId))
        case _ => Empty
      },
      urp => urp.userName :: urp.repoName :: urp.pullRequestId :: Nil) / * / * / "pull-request" / * >>
      ValueTemplate(up =>  up match {
        case Full(up1) => up1.repo match {
          case Full(repo) =>   Templates("repo" :: "pull-request" :: "one" :: Nil) openOr NodeSeq.Empty
          case _ => Templates("404" :: Nil) openOr NodeSeq.Empty
        }
        case _ => Templates("404" :: Nil) openOr NodeSeq.Empty
      })

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

    LiftRules.ajaxRetryCount = Full(1)
    LiftRules.ajaxPostTimeout = 15000

    def repoName_?(s: String) = s.endsWith(".git")

    LiftRules.statelessDispatchTable.prepend {
      case r @ Req(repo :: l, _, _) if(repoName_?(repo)) => () => Full(PermRedirectResponse("/git" + r.uri, r))
      case r @ Req(user :: repo :: l, _, _) if(repoName_?(repo) && user != "git") => () => Full(PermRedirectResponse("/git" + r.uri, r))
    }

    LiftRules.statelessRewrite.append {
      case RewriteRequest(ParsePath("index" :: Nil, _, _, true), _, _) =>

        RewriteResponse("user" :: "m" :: "signin" :: Nil, true)

      case RewriteRequest(ParsePath(user :: Nil, _, _, false), _, _) =>

        RewriteResponse("list" :: user :: Nil, Map[String, String]())

    }   

    

    LiftRules.liftRequest.append {
      case Req("git" :: _, _, _) => false
      case Req("images" :: _, _, _) => false
    }

    // Use jQuery 1.4
    LiftRules.jsArtifacts = net.liftweb.http.js.jquery.JQuery14Artifacts

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    LiftRules.htmlProperties.default.set((r: Req) => new Html5Properties(r.userAgent))

  }

}
