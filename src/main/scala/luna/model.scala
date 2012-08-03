package luna.model

import com.mongodb.casbah.Imports._

import com.novus.salat._
import com.novus.salat.json._
import com.novus.salat.global._
import com.novus.salat.annotations._
import com.novus.salat.dao.SalatDAO

import luna.props._

object PasswordHash {
  import org.mindrot.jbcrypt._

  val rounds = 12 

  def apply(pw: String) = Password(BCrypt.hashpw(pw, BCrypt.gensalt(rounds)))

}


case class Password(hash: String) {
  import org.mindrot.jbcrypt._

  def match_?(passwd: String) = BCrypt.checkpw(passwd, hash)
}

trait JSON {
  import net.liftweb.json._
  import net.liftweb.json.JsonDSL._

  object ObjectIdSerializer extends Serializer[ObjectId] {
    private val Class = classOf[ObjectId]

    def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), ObjectId] = {
      case (TypeInfo(Class, _), json) => new ObjectId((json \ "id").extract[String])
    }

    def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
      case x: ObjectId => JString(x.toString)
    }
  }

  private val defaultFormats = DefaultFormats + ObjectIdSerializer


  def asJValue = Extraction.decompose(this)(defaultFormats)
}

case class Hidden[A:Manifest](value: A)

case class User(
  @Key("_id") id: ObjectId = new ObjectId, 
  email: Option[String] = None, 
  login: String,
  password: Password,
  admin: Boolean = false,
  suspended: Boolean = false
) extends JSON

object User extends SalatDAO[User, ObjectId](
  collection = MongoConnection()(P.dbName)("users")
) {
  def byLogin(login: String) = findOne(MongoDBObject("login" -> login))

  def byId(id: ObjectId) = findOneByID(id)
  def byId(id: String) = findOneByID(new ObjectId(id))
}

case class RepositoryCommon(name: String, isPublic: Boolean)

case class Repository(
  @Key("_id") id: ObjectId = new ObjectId, 
  name: String, 
  isPublic: Boolean = true,
  ownerId: ObjectId
) extends JSON

object Repository extends SalatDAO[Repository, ObjectId](
  collection = MongoConnection()(P.dbName)("repositories")
) {
  def byOwnerId(id: ObjectId) = find(ref = MongoDBObject("id" -> id)).toList

  def byOwnerId(id: ObjectId, isPublic: Boolean) = find(ref = MongoDBObject("id" -> id, "isPublic" -> isPublic)).toList
}