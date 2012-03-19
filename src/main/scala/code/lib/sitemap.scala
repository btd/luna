package code.lib

import net.liftweb._
import sitemap._
import Loc._
import common._
import http._
import util.Helpers._

import xml.{Text, NodeSeq}

import code.model._

trait WithUser {
  def userName: String

  lazy val user = UserDoc.find("login", userName)
}

case class UserPage(userName: String) extends WithUser

object UserPage {
  def apply(u: UserDoc): UserPage = UserPage(u.login.get)
}

trait WithRepo extends WithUser {
  def repoName: String

  lazy val repo = user match {

    case Full(u) => tryo(u.repos.filter(_.name.get == repoName).head) match {
      case Full(r) if (r.canPull_?(UserDoc.currentUser)) => Full(r)
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

object RepoPage {
  def apply(r: RepositoryDoc): RepoPage = RepoPage(r.owner.login.get, r.name.get)
}

case class RepoAtCommitPage(userName: String, repoName: String, commit: String) extends WithCommit

case class PullRequestRepoPage(userName: String, repoName: String, pullRequestId: String)  extends WithPullRequest with WithRepo

object PullRequestRepoPage {
  def apply(pr: PullRequestDoc):PullRequestRepoPage = {
    val r = pr.destRepoId.obj.get
    PullRequestRepoPage(r.owner.login.get, r.name.get, pr.id.get.toString)
  }
}

case class SourceElementPage(userName: String, repoName: String, commit: String, path: List[String]) extends WithCommit {
  
  private lazy val reversedPath = path.reverse

  lazy val elem = repo.flatMap(r => 
    tryo { 
      path match {
        case Nil => r.git.ls_tree(Nil, commit)
        case _ => r.git.ls_tree(reversedPath.tail.reverse, commit).filter(_.basename == reversedPath.head).head 
      }
    })
}

object SourceElementPage {
  def apply(r: RepositoryDoc, commit: String): SourceElementPage = 
      SourceElementPage(r.owner.login.get, r.name.get, commit, Nil)
  
}



object Sitemap {
	val userRepos = Menu.param[WithUser]("userRepos",
	    new Loc.LinkText[WithUser](up => xml.Text("User " + up.userName)),
	    login => Full(UserPage(login)),
	    up => up.userName) / "list" / * >>
	    ValueTemplateBox(for(up <- _; u <- up.user; tpl <- Templates("list" :: Nil)) yield tpl)

	  val userAdmin = Menu.param[WithUser]("userAdmin",
	    new Loc.LinkText[WithUser](up => xml.Text("User " + up.userName)),
	    login => Full(UserPage(login)),
	    up => up.userName) / "admin" / *  >>
	    ValueTemplateBox(for(up <- _; u <- up.user; if u.is(UserDoc.currentUser); tpl <- Templates("admin" :: "adminUser" :: Nil)) yield tpl)

    val repoAdmin = Menu.params[WithRepo]("userRepoAdminPage",
      new LinkText[WithRepo](urp => Text("Repo " + urp.repoName)),
      list => list match {
        case login :: repo :: Nil => Full(RepoPage(login, repo))
        case _ => Empty
      },
      urp => urp.userName :: urp.repoName :: Nil) / "admin" / * / * >>
    ValueTemplateBox(for(rp <- _; u <- rp.user; r <- rp.repo if u.is(UserDoc.currentUser); tpl <- Templates("admin" :: "adminRepo" :: Nil)) yield tpl)

    val blobAtCommit = Menu.params[SourceElementPage]("blobAtCommit",
      new LinkText[SourceElementPage](stp => Text("Repo " + stp.repoName)),
      list => list match {
          case login :: repo :: commit :: path => Full(SourceElementPage(login, repo, commit, path))
          case _ => Empty
      },
      stp => stp.userName :: stp.repoName :: stp.commit :: stp.path) / * / * / "blob" / * / **  >>
      ValueTemplateBox(
        for {
          rp <- _
          repo <- rp.repo
          if repo.canPull_?(UserDoc.currentUser)
          elem <- rp.elem
          tpl <- Templates("repo" :: "blob" :: Nil)
        } yield tpl)

    val treeAtCommit = Menu.params[SourceElementPage]("treeAtCommit",
      new LinkText[SourceElementPage](stp => Text("Repo " + stp.repoName)),
      list => list match {
          case login :: repo :: commit :: path => Full(SourceElementPage(login, repo, commit, path))
          case _ => Empty
      },
      stp => stp.userName :: stp.repoName :: stp.commit :: stp.path) / * / * / "tree" / * / ** >>
      ValueTemplateBox(
        for {
          rp <- _
          repo <- rp.repo
          if repo.canPull_?(UserDoc.currentUser)
          elem <- rp.elem
          tpl <- Templates("repo" :: "tree" :: Nil)
        } yield tpl)				      

    val defaultTree = Menu.params[WithRepo]("defaultTree",
      new LinkText[WithRepo](stp => Text("Repo " + stp.repoName)),
      list => list match {
          case login :: repo :: Nil => Full(RepoPage(login, repo))
          case _ => Empty
      },
      stp => stp.userName :: stp.repoName :: Nil) / * / * / "tree" >>
      ValueTemplateBox(
        for {
          rp <- _
          r <- rp.repo
          if r.canPull_?(UserDoc.currentUser)
          tpl <- Templates("repo" :: "default" :: Nil)} yield tpl) >>
      TestValueAccess(
        for { 
          rp <- _
          r <- rp.repo
          if r.git.inited_?
        } yield RedirectResponse(treeAtCommit.calcHref(SourceElementPage(rp.userName, rp.repoName, r.git.currentBranch, Nil))))
    


    val historyAtCommit = Menu.params[SourceElementPage]("historyAtCommit",
      new LinkText[SourceElementPage](urp => Text("Repo " + urp.repoName)),
      list => list match {
        case login :: repo :: commit :: path => Full(SourceElementPage(login, repo, commit, path))
        case _ => Empty
      },
      stp => (stp.userName :: stp.repoName :: stp.commit :: Nil) ::: stp.path) / * / * / "commits" / * / ** >>
       ValueTemplateBox (
        for {
          rp <- _
          r <- rp.repo
          if r.canPull_?(UserDoc.currentUser)
          tpl <- Templates("repo" :: "commit" :: "all" :: Nil)} yield tpl)

    val defaultCommits = Menu.params[WithRepo]("defaultCommits",
      new LinkText[WithRepo](urp => Text("Repo " + urp.repoName)),
      list => list match {
        case login :: repo :: Nil => Full(RepoPage(login, repo))
        case _ => Empty
      },
      urp => urp.userName :: urp.repoName :: Nil) / * / * / "commits" >>
      ValueTemplateBox (
        for {
          rp <- _
          r <- rp.repo
          if r.canPull_?(UserDoc.currentUser)
          tpl <- Templates("repo" :: "commit" :: "default" :: Nil)} yield tpl) >>
      TestValueAccess(
        for {
          rp <- _
          r <- rp.repo
          if r.git.inited_?
        } yield RedirectResponse(historyAtCommit.calcHref(SourceElementPage(r, r.git.currentBranch))))  

    val commit = Menu.params[SourceElementPage]("commit",
      new LinkText[SourceElementPage](urp => Text("Repo " + urp.repoName)),
      list => list match {
        case login :: repo :: commit :: path => Full(SourceElementPage(login, repo, commit, path))
        case _ => Empty
      },
      stp => stp.userName :: stp.repoName :: stp.commit :: stp.path) / * / * / "commit" / * / ** >>
      ValueTemplateBox (
        for {
          rp <- _
          r <- rp.repo
          if r.canPull_?(UserDoc.currentUser)
          tpl <- Templates("repo" :: "commit" :: "one" :: Nil)} yield tpl)

    val pullRequests = Menu.params[WithRepo]("pullRequests",
      new LinkText[WithRepo](urp => Text("Repo " + urp.repoName)),
      list => list match {
        case login :: repo :: Nil => Full(RepoPage(login, repo))
        case _ => Empty
      },
      urp => urp.userName :: urp.repoName :: Nil) / * / * / "pull-requests" >>
      ValueTemplateBox (
        for {
          rp <- _
          r <- rp.repo
          if r.canPull_?(UserDoc.currentUser)
          tpl <- Templates("repo" :: "pull-request" :: "all" :: Nil)} yield tpl)

    val pullRequest = Menu.params[PullRequestRepoPage]("onePullRequestPage",
      new LinkText[PullRequestRepoPage](urp => Text("Repo " + urp.repoName)),
      list => list match {
        case login :: repo :: pullRequestId :: Nil => Full(PullRequestRepoPage(login, repo, pullRequestId))
        case _ => Empty
      },
      urp => urp.userName :: urp.repoName :: urp.pullRequestId :: Nil) / * / * / "pull-request" / * >>
      ValueTemplateBox (
        for {
          rp <- _
          r <- rp.repo
          if r.canPull_?(UserDoc.currentUser)
          tpl <- Templates("repo" :: "pull-request" :: "one" :: Nil)} yield tpl)

    val index = Menu.i("Home") / "index" >> 
      If(() => !UserDoc.loggedIn_?, () => RedirectResponse(userRepos.calcHref(UserPage(UserDoc.currentUser.get))))


    val signIn = Menu.i("Sign In") / "user" / "m" / "signin" >> 
          If(() => !UserDoc.loggedIn_?, () => RedirectResponse(userRepos.calcHref(UserPage(UserDoc.currentUser.get))))

    val login = Menu.i("Log In") / "user" / "m" / "login"

    val newUser = Menu.i("Registration") / "user" / "m" / "new"

    val notification = Menu.params[WithRepo]("notifyPushPage",
      new LinkText[WithRepo](urp => Text("Repo " + urp.repoName)),
      list => list match {
        case login :: repo :: Nil => Full(RepoPage(login, repo))
        case _ => Empty
      },
      urp => urp.userName :: urp.repoName :: Nil) / * / * / "notify" >>
      ValueTemplateBox(
        for {
          rp <- _
          r <- rp.repo
          if UserDoc.loggedIn_? && r.canPull_?(UserDoc.currentUser) //
          tpl <- Templates("notification" :: "push" :: Nil)} yield tpl)


   val entries = List[Menu](
      userRepos,
      userAdmin,
      repoAdmin,
      blobAtCommit,
      treeAtCommit,
      defaultTree,
      historyAtCommit,
      defaultCommits,
      commit,
      pullRequests,
      pullRequest,
      index,
      signIn,
      login,
      newUser,
      notification
    )

}