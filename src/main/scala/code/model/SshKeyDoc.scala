/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package code.model

import net.liftweb.mongodb.record.field.ObjectIdPk
import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.record.field.{StringField, TextareaField}
import net.liftweb.common.{Full}

/**
 * User: denis.bardadym
 * Date: 9/30/11
 * Time: 1:35 PM
 */

class SshKeyDoc private() extends MongoRecord[SshKeyDoc] with ObjectIdPk[SshKeyDoc] {

  object rawValue extends TextareaField(this, 4000)

  object ownerLogin extends StringField(this, 50)

  object ownerRepoName extends StringField(this, 50) {
    override def optional_? = true
  }



  //TODO заменить на один метод получающий все
  def for_user_? = ownerRepoName.valueBox match {
    case Full(_) => false
    case _ => true
  }

  def for_repo_?(repoName: String) = ownerRepoName.valueBox match {
    case Full(name) => repoName == name
    case _ => false
  }

  private lazy val splitedRawValue = rawValue.get.split(" ")

  lazy val comment = splitedRawValue(2)
  lazy val encodedKey = splitedRawValue(1)

  def meta = SshKeyDoc
}

object SshKeyDoc extends SshKeyDoc with MongoMetaRecord[SshKeyDoc] {
  override def collectionName: String = "ssh_keys"
}