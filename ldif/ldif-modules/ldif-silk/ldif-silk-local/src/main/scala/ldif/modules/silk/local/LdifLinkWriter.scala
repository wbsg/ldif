/* 
 * LDIF
 *
 * Copyright 2011-2013 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
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

package ldif.modules.silk.local

import ldif.runtime.QuadWriter
import ldif.entity.Node
import de.fuberlin.wiwiss.silk.output.LinkWriter
import ldif.runtime.Quad
import de.fuberlin.wiwiss.silk.entity.Link
import ldif.util.Consts

/**
 * A Silk Link Writer which writes all links to a LDIF Quad Writer.
 */
class LdifLinkWriter(quadWriter : QuadWriter, allowLinksWithSameURIs: Boolean = false) extends LinkWriter {

  def write(link: Link, predicateUri : String) {
    if(allowLinksWithSameURIs || link.source!=link.target)
      quadWriter.write(Quad(Node.createUriNode(link.source, null), predicateUri, Node.createUriNode(link.target, null), Consts.SILK_OUT_GRAPH))
  }
}
