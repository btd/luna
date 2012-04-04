package actor

import akka.kernel.Bootable
import akka.actor.ActorSystem

class LunaKernel extends Bootable {
  val system = ActorSystem("lunakernel")
 
  def startup = {
    
  }
 
  def shutdown = {
    system.shutdown()
  }
}