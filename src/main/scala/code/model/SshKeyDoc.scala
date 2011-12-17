/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package code.model

import net.liftweb._
import mongodb.record._
import field._
import common._
import record.field._
import util._
import Helpers._

/**
 * User: denis.bardadym
 * Date: 9/30/11
 * Time: 1:35 PM
 */

trait SshKey {
  def id: ObjectIdField[_]
  def rawValue: TextareaField[_]
  def ownerId: ObjectIdRefField[_,_]
}

trait SshKeyBase[T <: SshKeyBase[T]] extends MongoRecord[T] with ObjectIdPk[T] {
  self: T =>

  object rawValue extends TextareaField(this, 4000) {

    override def validations = valMinLen(1, "Ssh key cannot be empty") _ :: 
                              valRegex("""(?s)(ssh\-(?:dss|rsa)\s+\S+)(.*)""".r.pattern, "Ssh key is not valid") _ ::
                                                            super.validations
  }  

  def acceptableFor(r: RepositoryDoc): Boolean

  private lazy val splitedRawValue = rawValue.get.split(" ")

  lazy val algorithm = splitedRawValue(0)
  lazy val comment = tryo { splitedRawValue(2) } openOr { "" }
  lazy val encodedKey = splitedRawValue(1)
}

class SshKeyUserDoc extends SshKeyBase[SshKeyUserDoc] with SshKey {

  object ownerId extends ObjectIdRefField(this, UserDoc)

  def acceptableFor(r: RepositoryDoc) = {//if owned this repo
    ownerId.obj.map(o => o.id.get == r.owner.id.get) openOr false 
  }

  def meta = SshKeyUserDoc
}

object SshKeyUserDoc extends SshKeyUserDoc with MongoMetaRecord[SshKeyUserDoc] {
  override def collectionName: String = "ssh_keys_user"
}

class SshKeyRepoDoc extends SshKeyBase[SshKeyRepoDoc] with SshKey {

  object ownerId extends ObjectIdRefField(this, RepositoryDoc)

  def acceptableFor(r: RepositoryDoc) = {//if owned this repo
    ownerId.obj.map(o => o.id.get == r.id.get) openOr false 
  }

  def meta = SshKeyRepoDoc
}

object SshKeyRepoDoc extends SshKeyRepoDoc with MongoMetaRecord[SshKeyRepoDoc] {
  override def collectionName: String = "ssh_keys_repo"
}