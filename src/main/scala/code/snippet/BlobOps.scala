/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package code.snippet

import bootstrap.liftweb.SourceTreePage
import net.liftweb._
import common._
import util.Helpers._
import util._
import xml.Text

/**
 * User: denis.bardadym
 * Date: 10/4/11
 * Time: 12:49 PM
 */

class BlobOps(stp: SourceTreePage) extends Loggable {
  def renderSourceText = {
    stp.repo match {
      case Full(repo) => ".source_code" #> Text(repo.ls_cat(stp.path))
      case _ => PassThru
    }
  }
}