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

package ldif.local.datasources.crawl

import org.semanticweb.yars.nx.parser.Callback
import org.semanticweb.yars.nx.Node
import java.io.IOException
import ldif.runtime.QuadWriter
import ldif.runtime.Quad
import ldif.local.scheduler.CrawlImportJobPublisher

class CallbackQuadQueue(quadWriter : QuadWriter, reporter : CrawlImportJobPublisher = null) extends Callback {

  var statements = 0

  def startDocument() {}

  def endDocument() {}

  def processStatement(nodes : Array[Node]) {
    if (nodes.size > 2)
      try {
        quadWriter.write(Quad(ldif.entity.Node.fromNxNode(nodes(0)), nodes(1).toString, ldif.entity.Node.fromNxNode(nodes(2)),null))
      } catch {
        case e:IOException => {
          e.printStackTrace()
          throw new RuntimeException(e)
        }
      }
    statements += 1
    if (reporter != null)
      reporter.importedQuads.incrementAndGet()
  }

}