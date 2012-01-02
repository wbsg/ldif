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

package ldif.entity

import org.semanticweb.yars.nx.parser.NxParser
import ldif.util.NTriplesStringConverter
import ldif.util.Consts

final case class Node(value : String, datatypeOrLanguage : String, nodeType : Node.NodeType, graph : String) extends NodeTrait
{
  def modifyGraph(graph: String): Node = {
    Node(this.value, this.datatypeOrLanguage, this.nodeType, graph)
  }

  def compare(other: Node): Int = super.compare(other)
}

object Node
{
  val defaultGraph : String = Consts.DEFAULT_GRAPH

  def createLiteral(value : String, graph : String = null) = Node(value, null, Literal, graph)

  def createTypedLiteral(value : String, datatype : String, graph : String = null) = Node(value, datatype, TypedLiteral, graph)

  def createLanguageLiteral(value : String, language : String, graph : String = null) = Node(value, language, LanguageLiteral, graph)

   def createBlankNode(value : String, graph : String) = Node(NTriplesStringConverter.convertBnodeLabel(value), null, BlankNode, graph)

  def createUriNode(value : String, graph : String = null) = Node(value, null, UriNode, graph)

  def fromString(value : String, graph : String) = {
    val nxNode = NxParser.parseNode(value)
    fromNxNode(nxNode,graph)
  }

  def fromString(value : String) : Node = fromString(value, defaultGraph)

  def fromNxNode(nxNode : org.semanticweb.yars.nx.Node, graph : String) = {
     nxNode match {
      case lit:org.semanticweb.yars.nx.Literal => {
        val dt = lit.getDatatype
        val lang = lit.getLanguageTag
        val value = lit.getData
        if (dt!=null)
          ldif.entity.Node.createTypedLiteral(value,dt.toString,graph)
        else if (lang!=null)
          ldif.entity.Node.createLanguageLiteral(value,lang,graph)
        else ldif.entity.Node.createLiteral(value,graph)
      }
      case bno:org.semanticweb.yars.nx.BNode =>
        ldif.entity.Node.createBlankNode(bno.toString,graph)
      case res:org.semanticweb.yars.nx.Resource =>
        ldif.entity.Node.createUriNode(res.toString,graph)
    }
  }

  def fromNxNode(nxNode : org.semanticweb.yars.nx.Node) : Node = fromNxNode(nxNode,defaultGraph)

  // Build node from an XML node
  def fromXML(xml : scala.xml.Node) : Node = xml match  {
    //TODO Literal language and datatype not supported in M1
    case <Uri>{value @ _*}</Uri> => createUriNode(value.text,defaultGraph)
    case <Literal>{value @ _*}</Literal> => createLiteral(value.text,defaultGraph)
    case <BlankNode>{value @ _*}</BlankNode> => createBlankNode(value.text,defaultGraph)
  }

  sealed trait NodeType {val id: Int}

  case object Literal extends NodeType { val id = 1}

  case object TypedLiteral extends NodeType { val id = 2}

  case object LanguageLiteral extends NodeType { val id = 3}

  case object BlankNode extends NodeType { val id = 4}

  case object UriNode extends NodeType { val id = 5}

  case object NodeTypeMap {
    val map = Map(1 -> Literal, 2 -> TypedLiteral, 3 -> LanguageLiteral, 4 -> BlankNode, 5 -> UriNode)

    def apply(index: Int) = map(index)
  }
}
