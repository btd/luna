package luna.session

import luna.help._
import luna.props._
import luna.model._

import com.mongodb.casbah.Imports._ 

object Session extends Init {
  import com.google.common.cache._
  import java.util.concurrent._

  def newToken = java.util.UUID.randomUUID.toString

  private var loader = new CacheLoader[String, Option[ObjectId]] {
    def load(token: String) = None
  }

  private var cache: Option[LoadingCache[String, Option[ObjectId]]] = None

  def init {
    val ttl = P.sessionLifeTime

    cache = Some(CacheBuilder.from("expireAfterWrite=" + ttl)
                              .build(loader))
  }
  def isInited = !cache.isEmpty

  def put(data: ObjectId): Option[String] = {
    for {
      c <- cache
      token = newToken
    } yield {
      c.put(token, Some(data))
      token
    }
  }

  def get(token: String): Option[ObjectId] = cache.flatMap(_.get(token))
}