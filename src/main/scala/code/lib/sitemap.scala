package code.lib

import net.liftweb.sitemap._
import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.util.Helpers._
import Loc._

import xml.{Text, NodeSeq}

import code.model._



object Sitemap extends Loggable {
  import code.snippet.SnippetHelper._
  import Menu._


  /* ugly hack while Lift community ignore bug */
  implicit def toLoc[T](able: ParamsMenuable[T]): Loc[T] = {
    new Loc[T] with ParamExtractor[List[String], T] {
      def name = able.name    
      def headMatch: Boolean = able.headMatch
      def defaultValue = Empty
      def params = able.params
      def text = able.linkText
      val link = new ProxyLink(new ParamLocLink[T](able.path, able.headMatch, able.encoder))
      def locPath: List[LocPath] = able.path
      def parser = able.parser
      def listToFrom(in: List[String]): Box[List[String]] = Full(in)
      val pathLen = able.path.length
    }
    
  }

  //default LinkText's
  private val repoLinkText = new LinkText[RepositoryDoc](urp => Text("Repo " + urp.name.get))
  private val seLinkText = new LinkText[SourceElement](urp => Text("Source " + urp.pathLst.mkString("/")))

  private def parser(lst: List[String]): Box[RepositoryDoc] =
      lst match {
        case login :: repo :: Nil => RepositoryDoc.byUserLoginAndRepoName(login, repo)
        case _ => Empty
      }
  private def encoder(r: RepositoryDoc): List[String] = r.owner.login.get :: r.name.get :: Nil

  private def parserSE(lst: List[String]): Box[SourceElement] = {
    lst match {
      case userName :: repoName :: commit :: path => {
        RepositoryDoc.byUserLoginAndRepoName(userName, repoName).flatMap(
          SourceElement.find(_, commit, path)
        )
      }
      case _ => Empty
    }
     
  }

  private def encoderSE(se: SourceElement): List[String] = 
      se.repo.owner.login.get :: 
      se.repo.name.get :: 
      se.commit ::
      se.pathLst

  private def encoderPR(pr: PullRequestDoc): List[String] = {
      pr.destRepoId.obj.map(r => List[String](r.owner.login.get,r.name.get, pr.id.get.toString)).openOr(Nil)
  }

	  val userAdmin = Menu.param[UserDoc]("userAdmin",
	    new Loc.LinkText[UserDoc](up => xml.Text("User " + up.login.get)),
	    UserDoc.byName _,
	    _.login.get) / "admin" / *  >>
	    ValueTemplateBox(for(u <- _; if u.is(UserDoc.currentUser); tpl <- Templates("admin" :: "adminUser" :: Nil)) yield tpl)

    val repoAdmin = Menu.params[RepositoryDoc]("userRepoAdminPage",
      repoLinkText,
      parser _,
      encoder _) / "admin" / * / * >>
    ValueTemplateBox(for(r <- _; u = r.owner if u.is(UserDoc.currentUser); tpl <- Templates("admin" :: "adminRepo" :: Nil)) yield tpl)

    val userRepos = UserMenu()//Fuck >=(

    val blobAtCommit = Menu.params[SourceElement]("blobAtCommit",
      seLinkText,
      parserSE _,
      encoderSE _) / * / * / "blob" / * / **  >>
      ValueTemplateBox(
        for {
          se <- _
          if se.repo.canPull_?(UserDoc.currentUser)
          tpl <- Templates("repo" :: "blob" :: Nil)
        } yield tpl)

    val treeAtCommit = Menu.params[SourceElement]("treeAtCommit",
      seLinkText,
      parserSE _,
      encoderSE _) / * / * / "tree" / * / ** >>
      ValueTemplateBox(
        for {
          se <- _
          if se.repo.canPull_?(UserDoc.currentUser)
          tpl <- Templates("repo" :: "tree" :: Nil)
        } yield tpl)				   

    val defaultTree = Menu.params[RepositoryDoc]("defaultTree",
      repoLinkText,
      parser _,
      encoder _) / * / * / "tree" >>
      ValueTemplateBox(
        for {
          r <- _
          if r.canPull_?(UserDoc.currentUser)
          tpl <- Templates("repo" :: "default" :: Nil)} yield tpl) >>
      TestValueAccess(
        for { 
          r <- _
          if r.git.inited_?
        } yield RedirectResponse(treeAtCommit.calcHref(SourceElement.rootAt(r, r.git.currentBranch))))
    


    val historyAtCommit = Menu.params[SourceElement]("historyAtCommit",
      seLinkText,
      parserSE _,
      encoderSE _) / * / * / "commits" / * / ** >>
       ValueTemplateBox (
        for {
          se <- _
          if se.repo.canPull_?(UserDoc.currentUser)
          tpl <- Templates("repo" :: "commit" :: "all" :: Nil)} yield tpl)

    val defaultCommits = Menu.params[RepositoryDoc]("defaultCommits",
      repoLinkText,
      parser _,
      encoder _) / * / * / "commits" >>
      ValueTemplateBox (
        for {
          r <- _
          if r.canPull_?(UserDoc.currentUser)
          tpl <- Templates("repo" :: "commit" :: "default" :: Nil)} yield tpl) >>
      TestValueAccess(
        for {
          r <- _
          if r.git.inited_?
        } yield RedirectResponse(historyAtCommit.calcHref(SourceElement.rootAt(r, r.git.currentBranch))))

    val commit = Menu.params[SourceElement]("commit",
      seLinkText,
      parserSE _,
      encoderSE _) / * / * / "commit" / * / ** >>
      ValueTemplateBox (
        for {
          se <- _
          if se.repo.canPull_?(UserDoc.currentUser)
          tpl <- Templates("repo" :: "commit" :: "one" :: Nil)} yield tpl)

    val pullRequests = Menu.params[RepositoryDoc]("pullRequests",
      repoLinkText,
      parser _,
      encoder _) / * / * / "pull-requests" >>
      ValueTemplateBox (
        for {
          r <- _
          if r.canPull_?(UserDoc.currentUser)
          tpl <- Templates("repo" :: "pull-request" :: "all" :: Nil)} yield tpl)

    val pullRequest = Menu.params[PullRequestDoc]("onePullRequestPage",
      new LinkText[PullRequestDoc](urp => Text("Pull Request")),
      list => list match {
        case login :: repo :: pullRequestId :: Nil => {
          val pr = PullRequestDoc.find(pullRequestId)
          pr.flatMap(pullRequest => pullRequest.destRepoId.obj.filter(r => r.name.get == repo && r.owner.login.get == login).map(ignore => pullRequest))
        }
         
        case _ => Empty
      },
      encoderPR _) / * / * / "pull-request" / * >>
      ValueTemplateBox (
        for {
          pr <- _
          r <- pr.destRepoId.obj
          if r.canPull_?(UserDoc.currentUser)//TODO check
          tpl <- Templates("repo" :: "pull-request" :: "one" :: Nil)} yield tpl)

    val index = Menu.i("Home") / "index" >> 
      If(() => !UserDoc.loggedIn_?, () => RedirectResponse(userRepos.calcHref(UserDoc.currentUser.get)))


    val signIn: Menu = Menu("Sign In") / "user" / "m" / "signin" >> 
          If(() => !UserDoc.loggedIn_?, () => RedirectResponse(userRepos.calcHref(UserDoc.currentUser.get)))

    val login: Menu = Menu("Log In") / "user" / "m" / "login" >> 
          If(() => !UserDoc.loggedIn_?, () => RedirectResponse(userRepos.calcHref(UserDoc.currentUser.get)))

    val newUser: Menu = Menu("Registration") / "user" / "m" / "new" >> 
          If(() => !UserDoc.loggedIn_?, () => RedirectResponse(userRepos.calcHref(UserDoc.currentUser.get)))

    val adminUsers: Menu = Menu("Users") / "admin" / "users" >> 
      TemplateBox ( () =>
        for {
          cu <- UserDoc.currentUser
          if UserDoc.loggedIn_? && cu.admin.get
          tpl <- Templates("admin" :: "adminUsers" :: Nil)} yield tpl)

    val notification = Menu.params[RepositoryDoc]("notifyPushPage",
      repoLinkText,
      parser _,
      encoder _) / * / * / "notify" >>
      ValueTemplateBox(
        for {
          r <- _
          if UserDoc.loggedIn_? && r.canPull_?(UserDoc.currentUser) //
          tpl <- Templates("notification" :: "push" :: Nil)} yield tpl)


   

  def defaultEntries = List[Menu](
      index,
      userAdmin,
      repoAdmin,
      userRepos,
      blobAtCommit,
      treeAtCommit,
      defaultTree,
      historyAtCommit,
      defaultCommits,
      commit,
      pullRequests,
      pullRequest,
      login,
      notification,
      adminUsers
    )

  def entries = defaultEntries ::: List[Menu](
      signIn,
      newUser
    )

}

import Menu._

/* workarount to awoid weird links */
case class UserMenu() extends ParamMenuable[UserDoc](
  "userRepos", 
  new LinkText[UserDoc](user => xml.Text("User " + user.login.get)),
  UserDoc.byName _,
  _.login.get,
  * :: Nil,
  false,
  ValueTemplateBox((userBox:Box[UserDoc]) => for(u <- userBox; tpl <- Templates("list" :: Nil)) yield tpl) :: Nil,
  Nil
  )

  /* ugly hack while Lift community ignore bug */
class ProxyLink[-T](val self: Link[T]) extends Link[T](self.uriList , self.matchHead_? ) {

  override def isDefinedAt(req: Req): Boolean = isDefinedAt(req)

  override def pathList(value: T): List[String] = self.pathList(value)
  
  override def createPath(value: T): String = {
      val path: List[String] = pathList(value).map(urlEncode)

      if (matchHead_?) {
        path.mkString("/", "/", "")
      } else if (SiteMap.rawIndex_? && path == List("index")) {
        "/"
      } else if (path.length > 1 && path.last == "index") {
        path.dropRight(1).mkString("/", "/", "/")
      } else {
        path.mkString("/", "/", "")
      }
    }
}
