package luna.help

object H { 
  def tryo[A](f: => A): Option[A]  = 
    try {
      Some(f)
    } catch {
      case _ => None
    }
}

trait Loggable {
  val logger = org.slf4j.LoggerFactory.getLogger(this.getClass)
}

trait Init {
  
  def init: Unit

  def isInited: Boolean

}