/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package code.snippet

import bootstrap.liftweb.SourceElementPage
import net.liftweb._
import common._
import util.Helpers._
import util._
import xml.Text
import java.net.URLDecoder
import code.snippet.SnippetHelper._


/**
 * User: denis.bardadym
 * Date: 10/4/11
 * Time: 12:49 PM
 */

class BlobOps(stp: SourceElementPage) extends Loggable {
  def renderSourceText = {
    val decodedUrl: List[String] = stp.path.map(p => URLDecoder.decode(p, "UTF-8"))
    logger.debug("Try to render " + decodedUrl)
    stp.repo match {
       case Full(repo) => ".source_code" #> Text(repo.git.ls_cat(decodedUrl, stp.commit))
       case _ => PassThru
    }
  }

  def renderBranches = branchSelector(stp.repo, _ => stp.commit, _.sourceBlobUrl(_) + suffix(stp.path))
}