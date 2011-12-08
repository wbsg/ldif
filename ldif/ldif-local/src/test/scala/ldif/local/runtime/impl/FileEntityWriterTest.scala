/* 
 * LDIF
 *
 * Copyright 2011 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
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
import ldif.util.MemoryUsage
import ldif.entity.{EntityLocal, Node}
import java.io.File

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 10/23/11
 * Time: 7:44 PM
 * To change this template use File | Settings | File Templates.
 */

@RunWith(classOf[JUnitRunner])
class FileEntityWriterTest extends FlatSpec with ShouldMatchers {
  it should "keep the memory foot print constant" in {
    val startUsage = MemoryUsage.getMemoryUsage()
    val tmpFile = File.createTempFile("ldif-test", "test")
    tmpFile.deleteOnExit
    val writer = new FileEntityWriter(null, tmpFile)
    for(i <- 1 to 100000) {
      writer.write(new EntityLocal(Node.createUriNode("http://" + i, ""), null))
    }
    val endUsage = MemoryUsage.getMemoryUsage()
    writer.finish
    assert((endUsage-startUsage) < 2000)
  }
}