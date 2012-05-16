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
import org.slf4j.LoggerFactory
import xml.Node
import ldif.util.{Consts, CommonUtils}

case class SerializingQuadWriter(filepath: String, syntax: RDFSyntax) extends QuadWriter {
  var writer:BufferedWriter = null

  def initWriter() {
      writer = new BufferedWriter(new FileWriter(filepath))
  }

  def write(quad: Quad) {
    // Initialize the writer only when used for the first time
    if(writer==null)
      initWriter()
    syntax match {
      case NTRIPLES => writer.write(quad.toNTripleFormat)
      case NQUADS => writer.write(quad.toNQuadFormat)
    }
    writer.write(" .\n")
  }

  def finish() {
    if (writer!=null) {
      writer.flush()
      writer.close()
    }
  }
}

object SerializingQuadWriter{
  private val log = LoggerFactory.getLogger(getClass.getName)

  def fromXML(xml : Node) : Option[SerializingQuadWriter] = {
    val path = (xml text).trim//CommonUtils.getValueAsString(xml,"path").trim
    if (path == "") {
      log.warn("Invalid file output config. Please check http://www.assembla.com/code/ldif/git/nodes/ldif/ldif-core/src/main/resources/xsd/IntegrationJob.xsd")
      None
    }
    else {
      val outputFormat = CommonUtils.getAttributeAsString(xml,"format",Consts.FileOutputFormatDefault)

      // Get writable path, return None otherwise
      val outputPath = CommonUtils.getWritablePath(path).getOrElse({
        log.warn("Invalid file output. Unable to write to: "+ path )
        return None
      })

      if (outputFormat == "nquads")
        Some(SerializingQuadWriter(outputPath, NQUADS))
      else if (outputFormat == "ntriples")
        Some(SerializingQuadWriter(outputPath, NTRIPLES))
      else {
        log.warn("Output format not supported: "+ outputFormat )
        None
      }
    }
  }
}

sealed trait RDFSyntax {val name: String}

case object NTRIPLES extends RDFSyntax {val name = "N-Triples" }

case object NQUADS extends RDFSyntax { val name = "N-Quads"}

