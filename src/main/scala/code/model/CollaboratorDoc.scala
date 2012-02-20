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

import org.apache.commons.codec.digest.DigestUtils
import net.liftweb.record.field.{BooleanField, StringField}
import org.eclipse.jgit.lib.RepositoryCache
import org.eclipse.jgit.lib.RepositoryCache.FileKey
import java.io.File
import org.eclipse.jgit.util.FS
import net.liftweb.http.S
import net.liftweb.common.{Full, Box}
import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field.{ObjectIdRefField, ObjectIdPk}

class CollaboratorDoc  private() extends MongoRecord[CollaboratorDoc] with ObjectIdPk[CollaboratorDoc]  {
  object userId extends ObjectIdRefField(this, UserDoc)
  object repoId extends ObjectIdRefField(this, RepositoryDoc)

  def meta = CollaboratorDoc

}

object CollaboratorDoc extends CollaboratorDoc with MongoMetaRecord[CollaboratorDoc] {
  override def collectionName: String = "collaborators"


}