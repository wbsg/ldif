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

package ldif.local.runtime.impl

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import ldif.local.datasources.crawl.CrawlExecutor
import ldif.datasources.crawl.{CrawlModule, CrawlConfig}
import java.net.URI
import ldif.entity.Node
import ldif.local.runtime.QuadReader
import ldif.runtime.Quad

@RunWith(classOf[JUnitRunner])
class CrawlExecutorTest extends FlatSpec with ShouldMatchers {

  val executor = new CrawlExecutor
  val qq = new QuadQueue

    /* Disabled - remote test */

//  it should "load by crawling a resource URI" in {
//    executor.execute(task,null,qq)
//    qq.size should equal (1426)
//    contains(qq, testQuads) should equal (true)
//  }

  private lazy val task = {
    val config = new CrawlConfig(Traversable(new URI("http://dbpedia.org/resource/Stanley_Kubrick")))
    val module = new CrawlModule(config)
    module.tasks.head
  }

  // Create quads to check
  val testQuads = List(Quad(Node.createUriNode("http://dbpedia.org/resource/Stanley_Kubrick"),
      "http://dbpedia.org/ontology/birthPlace",
      Node.createUriNode("http://dbpedia.org/resource/Manhattan"),
      null))

  private def contains(qr : QuadReader, quads : Traversable[Quad]) =  {
    val isContained = new Array[Boolean](quads.size)
    while(qr.hasNext) {
      val quad = qr.read
      //println(quad.toString)
      for ((q,i) <- quads.toSeq.zipWithIndex)
        if (quad.equals(q))
          isContained(i) = true
    }
    isContained.filter(x => !x).isEmpty
  }
}