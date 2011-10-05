/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package code.snippet

import bootstrap.liftweb.SourcePage
import net.liftweb._
import common._
import http.{S, SHtml}
import util.Helpers._
import util._
import xml.Text
import java.net.URLDecoder
import code.model._


/**
 * User: denis.bardadym
 * Date: 10/4/11
 * Time: 12:49 PM
 */

class BlobOps(stp: SourcePage) extends Loggable {
  def renderSourceText = {
    val decodedUrl: List[String] = stp.path.map(p => URLDecoder.decode(p, "UTF-8"))
    stp.repo match {
      case Full(repo) => ".source_code" #> Text(repo.ls_cat(decodedUrl, stp.commit))
      case _ => PassThru
    }
  }

  def renderBranches = {
       stp.repo match {

          case Full(repo) => {
            //"#current_branch *" #> asScalaBuffer(branches).map(ref =>
            //  <option value={ref.getName.substring(ref.getName.lastIndexOf("/") + 1)} selected={if (stp.commit == ref.getName.substring(ref.getName.lastIndexOf("/") + 1)) "selected" else ""}>{ ref.getName.substring(ref.getName.lastIndexOf("/") + 1) } </option>
            //)
            "#current_branch" #>
              SHtml.ajaxSelect(repo.branches.zip(repo.branches),
                Full(stp.commit),
                commit => S.redirectTo(repo.sourceBlobUrl( commit) + (stp.path match {
                  case Nil => ""
                  case l => l.mkString("/", "/", "")
                })))

          }

          case _ => PassThru
        }

  }
}