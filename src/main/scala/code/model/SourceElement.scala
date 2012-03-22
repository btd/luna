/*
   Copyright 2012 Denis Bardadym

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package code.model

import net.liftweb.common._
import net.liftweb.util.Helpers._
import org.lunatool.linguist._

trait SourceElement {
   def name = pathLst.last

   def pathLst: List[String]

   def commit: String

   def repo: RepositoryDoc
}

case class Blob(repo: RepositoryDoc, commit: String, pathLst: List[String], size: Long) extends SourceElement with BlobHelper with FileBlob {
  def basePath = None//stat not need me

  def path = pathLst.mkString("/")

  def data = repo.git.ls_cat(pathLst, commit)
}

case class Tree(repo: RepositoryDoc, commit: String, pathLst: List[String]) extends SourceElement {

   /**
      list of source elements in Tree
   */
   def data = tryo(repo.git.ls_tree(pathLst, commit))
}

object SourceElement {
   def find(repo: RepositoryDoc, commit: String, path: List[String]): Box[SourceElement] = {  
      path match {
        case Nil => Full(Tree(repo, commit, Nil))
        case _ => tryo(repo.git.ls_tree(path.dropRight(1), commit).filter(_.pathLst.last == path.last).head)
      }
   }

   def rootAt(repo: RepositoryDoc, commit: String) = Tree(repo, commit, Nil)
}