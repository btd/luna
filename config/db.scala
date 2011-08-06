/**
 * Created by IntelliJ IDEA.
 * User: den
 * Date: 06.08.11
 * Time: 12:11
 * To change this template use File | Settings | File Templates.
 */
import com.twitter.querulous.config.Connection

new Connection {
  def url = "jdbc:h2:~/test"

  def username = "sa"

  def driver = "org.h2.Driver"

  def password = ""
}
