package daemon

trait Service {

	def shutdown(): Unit

	def init(): Unit
}