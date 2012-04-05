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
}