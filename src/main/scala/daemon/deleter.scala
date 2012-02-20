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