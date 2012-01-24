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

package ldif.output

import org.slf4j.LoggerFactory
import ldif.runtime.Quad

class FileWriter(filePath : String, f : String = "nq") extends OutputWriter {

  private val log = LoggerFactory.getLogger(getClass.getName)

  private val writer = new java.io.FileWriter(filePath)

  override def write(quad : Quad) {

    if(f == "nq")
        writer.write(quad.toNQuadFormat + " .\n")
     else
        writer.write(quad.toNTripleFormat + " .\n")

  }

  override def close() {
    writer.close()
  }


}