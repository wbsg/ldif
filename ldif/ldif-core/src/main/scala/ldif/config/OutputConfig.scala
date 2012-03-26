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

package ldif.config

import ldif.output._
import ldif.runtime.QuadWriter
import org.slf4j.LoggerFactory
import ldif.util.{Consts, CommonUtils}
import xml.{Elem, Node}

sealed trait IntegrationPhase {val name: String}
case object DT extends IntegrationPhase {val name = "Data translation" }
case object IR extends IntegrationPhase {val name = "Identity resolution" }
case object COMPLETE extends IntegrationPhase {val name = "Complete" }

class OutputConfig (val outputs : Traversable[(QuadWriter, IntegrationPhase)])
{
  def getByPhase(phase : IntegrationPhase) : Traversable[QuadWriter] =
      outputs.filter(_._2 == phase).map(_._1)

  override def toString = {
    var text = ""
    for (output <- outputs) {
      output._1 match {
        case s:SparqlWriter => text += s.uri
        case f:SerializingQuadWriter => text += f.filepath
        case _ =>
      }
      text += " ("+ output._2.name+") "
    }
    text
  }
}

object OutputConfig {

  private val log = LoggerFactory.getLogger(getClass.getName)

  // use this on an outputs element
  def fromOutputsXML(outputsNode : Node) : OutputConfig =
    new OutputConfig((outputsNode \ "output").map(parseOutput(_)))

  // Use this on an output element
  def fromOutputXML(outputNode: Node) : OutputConfig =
    new OutputConfig(Seq(parseOutput(outputNode)))

  private def parseOutput(xml : Node) : (QuadWriter, IntegrationPhase) =
    (parseOutputWriter(xml.child.filter(_.isInstanceOf[Elem]).head), parseOutputPhase((xml \ "phase").filter(_.isInstanceOf[Elem]).headOption))

  private def parseOutputWriter (xml : Node) : QuadWriter =
    if (xml.label == "sparql")
      getSparqlWriter(xml)
    else getFileWriter(xml)

  private def parseOutputPhase (xml : Option[Node]) : IntegrationPhase = {
    val phase = xml match {
      case Some(node) =>  node.text.trim.toLowerCase
      case None =>  Consts.OutputPhaseDefault
    }
   phase match {
     case "r2r" => DT   // TODO change names?
     case "silk" => IR
     case _ => COMPLETE
   }
  }

  private def getSparqlWriter(xml : Node) : SparqlWriter = {
    val endpointURI =  CommonUtils.getValueAsString(xml,"endpointURI")
    if (endpointURI == "") {
      log.warn("Invalid SPARQL output config. Please check http://www.assembla.com/code/ldif/git/nodes/ldif/ldif-core/src/main/resources/xsd/IntegrationJob.xsd")
      null
    }
    else {
      val user = CommonUtils.getValueAsString(xml,"user")
      val password = CommonUtils.getValueAsString(xml,"password")
      val sparqlVersion = CommonUtils.getValueAsString(xml,"sparqlVersion", Consts.SparqlUpdateVersionDefault)
      val useDirectPost = CommonUtils.getValueAsString(xml,"useDirectPost", Consts.SparqlUseDirectPostDefault).toLowerCase.equals("true")
      val queryParameter = CommonUtils.getValueAsString(xml,"queryParameter", Consts.SparqlQueryParameterDefault)
      SparqlWriter(endpointURI, Some(user, password), sparqlVersion, useDirectPost, queryParameter)
    }
  }

  private def getFileWriter(xml : Node) : SerializingQuadWriter = {
    val outputUriOrPath = CommonUtils.getValueAsString(xml,"path").trim
    if (outputUriOrPath == "") {
      log.warn("Invalid file output config. Please check http://www.assembla.com/code/ldif/git/nodes/ldif/ldif-core/src/main/resources/xsd/IntegrationJob.xsd")
      null
    }
    else {
      val outputFormat = CommonUtils.getValueAsString(xml,"format",Consts.FileOutputFormatDefault)
      if (outputFormat == "nquads")
        new SerializingQuadWriter(outputUriOrPath, NQUADS)
      else if (outputFormat == "ntriples")
        new SerializingQuadWriter(outputUriOrPath, NTRIPLES)
      else {
        log.warn("Output format not supported: "+ outputFormat )
        null
      }
    }
  }
}