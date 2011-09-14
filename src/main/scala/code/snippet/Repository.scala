/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package code.snippet

import net.liftweb._
import http._
import util.Helpers._

/**
 * User: denis.bardadym
 * Date: 9/14/11
 * Time: 3:43 PM
 */

class Repository {
  def create = {
       "button" #> SHtml.button("Create repository", processCreation)
  }

  def processCreation() = {

  }

  def list = {}
}