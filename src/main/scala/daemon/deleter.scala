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
package daemon

import org.quartz._

import net.liftweb.common.Loggable
import net.liftweb.util.Props

import code.model.RepositoryDoc

import java.io.{File, IOException}

import org.apache.commons.io.FileUtils

class DeleteUnneededReposJob extends Job with Loggable {

	lazy val fsDir = new File(Props.get(main.Constants.REPOSITORIES_DIR, "./repo/"));

	def execute(context: JobExecutionContext) {
		logger.debug("Deleting repos job started")

		val listOfFiles = fsDir.list.toSet

		val reposNames = RepositoryDoc.all.map(_.fsName.get).toSet

		val filesToDelete = listOfFiles &~ reposNames

		for { 
			repo <- filesToDelete
			repoDir = new File(fsDir, repo)
		} {
			logger.debug("Try to remove: " + repo)
			try {
				FileUtils.deleteDirectory(repoDir)
			} catch {
				case e: IOException => logger.warn("Exception while remove " + repo, e)
			}
		}

		logger.debug("Deleting repos job finished")
	}	
}