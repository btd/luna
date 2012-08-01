package luna.boot

import javax.servlet._

import luna.props._
import luna.session._
 
class Boot extends ServletContextListener {
  
  def contextInitialized( contextEvent: ServletContextEvent) {
    Config.init
    P.init
    Session.init
  }

  def contextDestroyed( contextEvent: ServletContextEvent) {
    
  }
}