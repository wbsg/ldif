/*
 * LDIF
 *
 * Copyright 2011-2012 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ldif.util

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 2/21/12
 * Time: 12:20 PM
 * To change this template use File | Settings | File Templates.
 */

/**
 * Objects of this trait give text and HTML representations for a REST interface.
 */
trait StatusMonitor {
  def getHtml: String = getHtml(Map())

  def getHtml(params: Map[String, String]): String

  def getText: String
  
  def addHeader(title : String, params: Map[String, String]): String = {
    val sb = new StringBuilder
    sb.append("<html><head><title>")
    sb.append(title)
    sb.append("</title>")
    sb.append(addParams(params))
    sb.append(addStyle)
    sb.append("</head><body>\n")
    sb.toString
  }

  def addParams(params: Map[String, String]): String = {
    val sb = new StringBuilder
    if(params.get("refresh").get!="0") {
      sb.append("<meta http-equiv=\"refresh\" content=\"")
      sb.append(params.get("refresh").get)
      sb.append("\">")
    }
    sb.toString
  }

  def addStyle() : String = {
  val sb = new StringBuilder
  sb.append("<style>")
  sb.append("body { background: white; color: black; font-family: sans-serif; line-height: 1.3em; margin: 0; padding: 2.5em 3em; }\n" +
    "table {border-collapse: collapse; margin-bottom: .75em;}\n" +
    "table th {text-align: left; background: #EAF3FA; padding: .4em .8em; height: 20px; font-weight: bold; font-size: 1em; color: #333}\n" +
    "table th a {text-decoration: none !important}\n" +
    "table td {vertical-align: top; text-align: left; padding: .4em 1em;}\n" +
    "table tr.even {background-color: #fff}\n" +
    "table tr.odd {background-color: #f6f6f6}\n" +
    "a:link, a:visited, a:hover, a:active { color: #1C5489; display: block; text-decoration: none; }\n" +
    "a:hover, a:active { color: #16426B; cursor: pointer; }\n" +
    "h1, h2, h3, h4 { color: #800; clear: both; }\n")
  sb.append("</style>")
  sb.toString
  }

  def buildCell(text : String) = "<td>"+text+"</td>"
}