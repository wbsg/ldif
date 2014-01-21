/*
 * LDIF
 *
 * Copyright 2011-2014 Universität Mannheim, MediaEvent Services GmbH & Co. KG
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
import ldif.util.Consts
import xml.{Elem, Node}
import org.slf4j.LoggerFactory

sealed trait IntegrationPhase {val name: String}
case object DT extends IntegrationPhase {val name = "Data translation" }
case object IR extends IntegrationPhase {val name = "Identity resolution" }
case object COMPLETE extends IntegrationPhase {val name = "Complete" }

class OutputConfig (val outputs : Traversable[(Option[QuadWriter], IntegrationPhase)])
{
  def getByPhase(phase : IntegrationPhase) : Traversable[QuadWriter] =
    validOutputs.filter(_._2 == phase).map(_._1.get)

  def validOutputs = outputs.filter(_._1 != None)

  override def toString = {
    var text = ""
    for (output <- validOutputs) {
      output._1.get match {
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

  private def parseOutput(xml : Node) : (Option[QuadWriter], IntegrationPhase) =
    (parseOutputWriter(xml.child.filter(_.isInstanceOf[Elem]).head), parseOutputPhase((xml \ "phase").filter(_.isInstanceOf[Elem]).headOption))

  private def parseOutputWriter (xml : Node) : Option[QuadWriter] =
    if (xml.label == "sparql")
      SparqlWriter.fromXML(xml)
    else SerializingQuadWriter.fromXML(xml)

  private def parseOutputPhase (xml : Option[Node]) : IntegrationPhase = {
    val phase = xml match {
      case Some(node) =>  node.text.trim.toLowerCase
      case None =>  Consts.OutputPhaseDefault
    }
   phase match {
     case "r2r" => DT
     case "silk" => IR
     case _ => COMPLETE
   }
  }
}