package ldif.util

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 2/21/12
 * Time: 12:20 PM
 * To change this template use File | Settings | File Templates.
 */

trait StatusMonitor {
  def getHtml: String = getHtml(Map())

  def getHtml(params: Map[String, String]): String

  def getText: String

  def addParams(params: Map[String, String]): String = {
    val sb = new StringBuilder
    if(params.get("refresh").get!="0") {
      sb.append("<meta http-equiv=\"refresh\" content=\"")
      sb.append(params.get("refresh").get)
      sb.append("\">")
    }
    sb.toString
  }
}