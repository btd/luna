package luna.wiki

import luna.help._
import luna.props._

object Wiki {
  import H._

  def welcomeFileContent = tryo(new java.util.Scanner( new java.io.File(P.welcomePage) ).useDelimiter("\\A").next)
  private val processor = new org.pegdown.PegDownProcessor

  def processContent(c: String): String = processor.markdownToHtml(c)

  lazy val processedContent = welcomeFileContent.map(processContent(_))

  def finalContent = (if(P.devMode) welcomeFileContent.map(processContent(_)) else processedContent).getOrElse("Hello, this is Luna!")

}