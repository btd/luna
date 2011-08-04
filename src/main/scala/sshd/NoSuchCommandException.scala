package sshd

/**
 * Created by IntelliJ IDEA.
 * User: denis.bardadym
 * Date: 8/4/11
 * Time: 1:05 PM
 * To change this template use File | Settings | File Templates.
 */

class NoSuchCommandException(message : String) extends Exception {
  override def getMessage = message
}