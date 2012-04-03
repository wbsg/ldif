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

import ldif.local.runtime.QuadReader
import ldif.runtime.{Quad, QuadWriter}
import ldif.util.Consts

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 3/19/12
 * Time: 3:35 PM
 * To change this template use File | Settings | File Templates.
 */

/**
 *
 */
class CopyQuadsQuadReader(inputQuadReader: QuadReader, filterTerms: Set[String], filteredQuads: QuadWriter) extends QuadReader{
  def size = inputQuadReader.size

  /**
   * If the class or the property of a quad is found in filterTerms then output to quad writer
   */
  def read(): Quad = {
    val quad = inputQuadReader.read()
    if(quad.predicate==Consts.RDFTYPE_URI && filterTerms.contains(quad.value.value))
      filteredQuads.write(quad)
    else if(filterTerms.contains(quad.predicate))
      filteredQuads.write(quad)

    return quad
  }

  def hasNext = inputQuadReader.hasNext
}