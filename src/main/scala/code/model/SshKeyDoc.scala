/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package code.model

import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.record.field.{StringField, TextareaField}
import net.liftweb.common.{Full}
import net.liftweb.mongodb.record.field.{ObjectIdRefField, ObjectIdPk}

/**
 * User: denis.bardadym
 * Date: 9/30/11
 * Time: 1:35 PM
 */

class SshKeyDoc private() extends MongoRecord[SshKeyDoc] with ObjectIdPk[SshKeyDoc] {

  object rawValue extends TextareaField(this, 4000)

  object ownerId extends ObjectIdRefField(this, UserDoc)

  object ownerRepoId extends ObjectIdRefField(this, RepositoryDoc) {
    override def optional_? = true
  }

  def acceptableFor_?(repo: RepositoryDoc) = ownerRepoId.obj match {
    case Full(r) => r.name == repo.name //ключ репозитория    TODO надо здесь get??
    case _ => true //ключ пользователя
  }

  private lazy val splitedRawValue = rawValue.get.split(" ")

  lazy val comment = splitedRawValue(2)
  lazy val encodedKey = splitedRawValue(1)

  def meta = SshKeyDoc
}

object SshKeyDoc extends SshKeyDoc with MongoMetaRecord[SshKeyDoc] {
  override def collectionName: String = "ssh_keys"
}