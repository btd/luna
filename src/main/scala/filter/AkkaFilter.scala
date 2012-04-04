package filter

import org.slf4j.LoggerFactory

import akka.kernel.Bootable

import javax.servlet.{ServletRequest, ServletResponse, FilterChain, FilterConfig}

/**
	Filter to run bootable akka application

	@param bootable - full class name which implements akka.kernel.Bootable
*/
class AkkaFilter extends javax.servlet.Filter {

	private val logger = LoggerFactory.getLogger(this.getClass)

	var bootable: Option[Bootable] = _

	def destroy() {
		for (boot <- bootable) {
			logger.info("Shuting down " + boot.getClass.getName)
			boot.shutdown()
		}
	}
	def doFilter(request: ServletRequest, response: ServletResponse , chain: FilterChain) {
		chain.doFilter(request, response)
	}

	def init(filterConfig: FilterConfig) {
		val bootableClass = Option(filterConfig.getInitParameter("bootable"))

		if(bootableClass.isEmpty) {
			logger.error("Bootable class not specified")
		} else {
			val classLoader = Thread.currentThread.getContextClassLoader

    		bootable = bootableClass map { 
    			classLoader.loadClass(_).newInstance.asInstanceOf[Bootable] 
    		}
    		
    		for (boot <- bootable) {
		      logger.info("Starting up " + boot.getClass.getName)
		      boot.startup()
		    }

    		logger.info("Successfully started Akka")
		}
	}

}