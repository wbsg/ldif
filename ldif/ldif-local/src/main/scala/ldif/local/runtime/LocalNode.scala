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

package ldif.local.runtime

import ldif.local.util.StringPool
import com.hp.hpl.jena.rdf.model.{Resource, Literal, RDFNode}
import ldif.util.Consts
import java.util.Properties
import ldif.entity.{NodeTrait, Node}

/*
* Object used to create Node using string canonicalization
*/

object LocalNode
{
  val defaultGraph : String = Consts.DEFAULT_GRAPH
  private var useStringPool = true
  private var useUriCompression = true

  def createResourceNode(value : String, graph : String) = {
    if (!value.startsWith("_:") && !value.startsWith("<")) {
      // Add brackets to SPARQL result URIs
      Node.fromString("<"+intern(value)+">", intern(graph))
    }
    else
      Node.fromString(intern(value), intern(graph))
  }

  def reconfigure(config: Properties) {
    val ebType = config.getProperty("entityBuilderType", "in-memory").toLowerCase
    if(ebType=="quad-store")
      useStringPool = false
    else
      useStringPool = true
  }

  def setUseStringPool(on: Boolean) {
    useStringPool = on
  }

  def setUseUriCompression(on: Boolean) {
    useUriCompression = on
  }

  // Intern a Node (eventually compressed)
  def intern(node : NodeTrait) : Node = {
    var value = node.value
    if (node.isResource)
      value = intern(node.value)
    Node(value, intern(node.datatypeOrLanguage), node.nodeType, intern(node.graph))
  }

  // Decompress (and eventually intern) a Node
  def decompress(node: Node) : Node =
    if (useUriCompression){
      var value = node.value
      if (node.isResource)
        value = decompress(node.value)
      Node(value, decompress(node.datatypeOrLanguage), node.nodeType, decompress(node.graph))
    }
    else node

  // Intern a String (eventually compressed)
  private def intern(str: String, compress : Boolean = useUriCompression) : String =
    if(useStringPool)
      StringPool.getCanonicalVersion(str, compress)
    else str

  // Decompress (and eventually intern) a String
  private def decompress(str:String) : String =
    intern(StringPool.decompress(str), false)


  private def convertLiteralNode(node: RDFNode, graphURI: String): Node = {
    val lexicalValue = node.asInstanceOf[Literal].getLexicalForm
    val datatype = node.asInstanceOf[Literal].getDatatypeURI
    val language = node.asInstanceOf[Literal].getLanguage

    if (datatype != null)
      Node.createTypedLiteral(lexicalValue, datatype, graphURI)
    else if (language != "")
      Node.createLanguageLiteral(lexicalValue, language, graphURI)
    else
      Node.createLiteral(lexicalValue, graphURI)
  }

  def fromRDFNode(node: RDFNode, graphURI: String = defaultGraph): NodeTrait = {
    if(node.isURIResource) {
      Node.createUriNode(node.asInstanceOf[Resource].getURI, graphURI)
    } else if(node.isLiteral) {
      convertLiteralNode(node, graphURI)
    } else if(node.isAnon) {
      Node.createBlankNode(node.asInstanceOf[Resource].getId.getLabelString, graphURI)
    } else
      throw new RuntimeException("Unknown node type for RDFNode: " + node) // Should never be the case

  }


}