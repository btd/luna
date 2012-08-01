package luna.session

import luna.help._
import luna.props._
import luna.model._

case class SessionData(user: Option[User])

object Session extends Init {
  import com.google.common.cache._
  import java.util.concurrent._

  def newToken = java.util.UUID.randomUUID.toString

  private var loader = new CacheLoader[String, SessionData] {
    def load(token: String) = SessionData(None)
  }

  private var cache: Option[LoadingCache[String, SessionData]] = None

  def init {
    val ttl = P.sessionLifeTime

    cache = Some(CacheBuilder.from("expireAfterWrite=" + ttl)
                              .build(loader))
  }
  def isInited = !cache.isEmpty

  def put(data: SessionData): Option[String] = {
    for {
      c <- cache
      token = newToken
    } yield {
      c.put(token, data)
      token
    }
  }

  def get(token: String) = cache.get(token)
}