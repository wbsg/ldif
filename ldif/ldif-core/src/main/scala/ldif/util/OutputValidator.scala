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

package ldif.util

import java.io.File
import org.slf4j.LoggerFactory
import ldif.runtime.Quad
import ldif.datasources.dump.QuadParser

/*
* LDIF output validation helper
*/

object OutputValidator {

  private val log = LoggerFactory.getLogger(getClass.getName)

  def contains(ldifOutputFile:File, quads : Traversable[Quad]) : Boolean = {
    val parser = new QuadParser
    val lines = scala.io.Source.fromFile(ldifOutputFile).getLines
    contains(lines.toTraversable.map(parser.parseLine(_)),quads)
  }

  def contains(quads : Traversable[Quad], testQuads : Traversable[Quad], testQuadsAreCorrect : Boolean = true) : Boolean = {
    val isContained = new Array[Boolean](testQuads.size)

    for (oq <- quads){
      for ((q,i) <- testQuads.toSeq.zipWithIndex)
        if (oq.equals(q))
          isContained(i) = true
    }

    if (testQuadsAreCorrect)
      isContained.filter(x => !x).isEmpty
    else
      isContained.filter(x => x).isEmpty
  }

  def containsNot(quads : Traversable[Quad], testQuads : Traversable[Quad]) = {
    contains(quads, testQuads, false)
  }

}
