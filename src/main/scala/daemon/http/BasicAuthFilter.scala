package daemon.http

import javax.servlet._
import javax.servlet.http._

import org.apache.commons.codec.binary.Base64

import code.model.UserDoc

class BasicAuthFilter extends Filter {

	var realmName: String = _

	def destroy() {}

	def init(filterConfig: FilterConfig) {
		realmName = filterConfig.getInitParameter("realm")
	}

	def doFilter(req: ServletRequest , res: ServletResponse, chain: FilterChain) {
		val request = req.asInstanceOf[HttpServletRequest]
        val response = res.asInstanceOf[HttpServletResponse]

        val user = authPassed_?(request)

		user match {
			case Some(u) => {
				val wrapped = new HttpServletRequestWrapper(request)
				wrapped.setAttribute("username", u.login.get)
				wrapped.setAttribute("email", u.email.get)
    			chain.doFilter(wrapped, response)
			}
			case _ => {
				response.addHeader("WWW-Authenticate", "Basic realm=\"" + realmName + "\"")
        		response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not authenticated")
			}
		}
	} 

	def authPassed_?(request: HttpServletRequest): Option[UserDoc] = {
		val header = request.getHeader("Authorization")

		if (header == null || !header.startsWith("Basic ")) {
            None
        } else {
        	val (username, password) = extractAndDecodeHeader(header)

        	UserDoc.byName(username).filter(u => u.password.match_?(password))
        }
	}

	def extractAndDecodeHeader(header: String): (String, String) = {
        val base64Token = header.substring(6).getBytes("UTF-8") 
        
        val token = new String(Base64.decodeBase64(base64Token), "UTF-8")

        val tokens = token.split(":")
        
        tokens(0) -> tokens(1)
    }
	
}