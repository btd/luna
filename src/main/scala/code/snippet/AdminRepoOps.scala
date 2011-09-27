/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package code.snippet

import bootstrap.liftweb.UserRepoPage
import xml.Text
import net.liftweb._
import util.Helpers._
import http._
import common._

/**
 * User: denis.bardadym
 * Date: 9/27/11
 * Time: 3:28 PM
 */

class AdminRepoOps(urp: UserRepoPage)  extends Loggable {
  def collaborators = {
    "*" #> <b>{"This is " + urp.login + " " + urp.repoName}</b>
  }
}