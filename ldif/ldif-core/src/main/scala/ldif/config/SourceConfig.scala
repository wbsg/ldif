/*
 * LDIF
 *
 * Copyright 2011-2014 Universität Mannheim, MediaEvent Services GmbH & Co. KG
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
package ldif.config

import xml.Node
import ldif.util.CommonUtils

object SourceConfig {

  def fromSourcesXML(xml : Node) : Traversable[String] =
    (xml \ "source").filter(_.text != "").map(parseSource(_))

  def fromSourceNode(sourceNode : Node) : Traversable[String] =
    Seq(sourceNode).filter(_.text != "").map(parseSource(_))

  private def parseSource(node : Node) : String =
    CommonUtils.getFilePath(node.text.trim)
}