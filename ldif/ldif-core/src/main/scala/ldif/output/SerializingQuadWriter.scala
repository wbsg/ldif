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

import ldif.runtime.{Quad, QuadWriter}
import java.io.{FileWriter, BufferedWriter}

class SerializingQuadWriter(val filepath: String, val syntax: RDFSyntax) extends QuadWriter {
  var writer:BufferedWriter = new BufferedWriter(new FileWriter(filepath))

  def write(quad: Quad) {
    syntax match {
      case NTRIPLES => writer.write(quad.toNTripleFormat)
      case NQUADS => writer.write(quad.toNQuadFormat)
    }
    writer.write(" .\n")
  }

  def finish() {writer.flush(); writer.close()}
}

sealed trait RDFSyntax {val name: String}

case object NTRIPLES extends RDFSyntax {val name = "N-Triples" }

case object NQUADS extends RDFSyntax { val name = "N-Quads"}

