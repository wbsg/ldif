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

package de.fuberlin.wiwiss.r2r

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 27.04.11
 * Time: 17:39
 * To change this template use File | Settings | File Templates.
 */

import ldif.entity.{NodeTrait, Node}
import ldif.runtime.QuadWriter
import de.fuberlin.wiwiss.r2r.TripleElement.Type
import scala.collection.JavaConversions._
import de.fuberlin.wiwiss.r2r.functions.HelperFunctions
import collection.mutable.ArrayBuffer
import ldif.util.Consts._
import ldif.runtime.Quad

class LDIFTargetPattern(targetPattern: TargetPattern) extends TargetPattern(targetPattern.getPath) {

  def writeQuads(results: LDIFVariableResults, quadWriter: QuadWriter) {
    for(triple <- getPath) {
      val subjects = getSubjects(triple.getSubject, results)
      val predicate = getPredicate(triple.getVerb)
      val objects = getObjects(triple.getObject, results)
      for(s <- subjects)
        for(o <- objects)
          quadWriter.write(createQuad(s, predicate, o))
    }
  }

  def createQuads(variableResults: LDIFVariableResults): Iterable[Quad] = {
    val results = new ArrayBuffer[Quad]

    for(triple <- getPath) {
      val subjects = getSubjects(triple.getSubject, variableResults)
      val predicate = getPredicate(triple.getVerb)
      val objects = getObjects(triple.getObject, variableResults)
      for(s <- subjects)
        for(o <- objects)
          results.append(createQuad(s, predicate, o))
    }
    return results
  }

  private def createQuad(s: NodeTrait, predicate: NodeTrait, o: NodeTrait): Quad = {
    var graph: String = DEFAULT_GRAPH
    if(o.graph!=null && o.graph!=DEFAULT_GRAPH && o.graph!="")
      graph = o.graph
    else if (s.graph!=null && s.graph!=DEFAULT_GRAPH && s.graph!="")
      graph = s.graph
    return Quad(s, predicate.value, o, graph)
  }

  private def getObjects(tripleElement: TripleElement, results: LDIFVariableResults): Iterable[NodeTrait] = {
    val elemType = tripleElement.getType
    val lexValue = tripleElement.getValue(0)

    val objects: List[NodeTrait] = elemType match {
      case Type.IRI=> List(Node.createUriNode(lexValue, DEFAULT_GRAPH))
      case Type.IRIVARIABLE => for(Node(uri, _, _, graph) <- results.getResults(lexValue).get) yield Node.createUriNode(uri, graph)
      case Type.DATATYPEVARIABLE => getDataTypeVariableValues(tripleElement,results)
      case Type.DATATYPESTRING => List(Node.createTypedLiteral(lexValue, tripleElement.getValue(1), DEFAULT_GRAPH))
      case Type.STRING => List(Node.createLiteral(lexValue, DEFAULT_GRAPH))
      case Type.STRINGVARIABLE => convertNodesToLiteralNodes(results.getResults(tripleElement.getValue(0)).get)
      case Type.LANGTAGVARIABLE => for(Node(value, _, _, graph) <- results.getResults(tripleElement.getValue(0)).get)
                                      yield Node.createLanguageLiteral(value, tripleElement.getValue(1), graph)
      case Type.LANGTAGSTRING => List(Node.createLanguageLiteral(lexValue, tripleElement.getValue(1), DEFAULT_GRAPH))
      case Type.VARIABLE => results.getResults(tripleElement.getValue(0)).get
      case Type.BLANKNODE => List(results.getBlankNode(lexValue, DEFAULT_GRAPH))
      case _ => {
        val dataType = elemType match {
          case Type.BOOLEAN => "http://www.w3.org/2001/XMLSchema#boolean"
          case Type.DECIMAL => "http://www.w3.org/2001/XMLSchema#decimal"
          case Type.INTEGER => "http://www.w3.org/2001/XMLSchema#integer"
          case Type.DOUBLE => "http://www.w3.org/2001/XMLSchema#double"
        }
        List(Node.createTypedLiteral(lexValue, dataType, DEFAULT_GRAPH))
      }
    }
    objects
  }

  private def convertToLiteral(element: NodeTrait): NodeTrait = {
    Node.createLiteral(element.value, element.graph)
  }

  private def convertToUri(element: NodeTrait): NodeTrait = {
    Node.createUriNode(element.value, element.graph)
  }

  private def convertNodesToUriNodes(nodes: List[NodeTrait]): List[NodeTrait] = {
    nodes map convertToUri
  }

  private def convertNodesToLiteralNodes(nodes: List[NodeTrait]): List[NodeTrait] = {
    nodes map convertToLiteral
  }

  private def getDataTypeVariableValues(element: TripleElement, results: LDIFVariableResults): List[NodeTrait] = {
    val varName = element.getValue(0)
    var values = results.getResults(varName).get
    val hint = getHints.get(varName)

    if(HelperFunctions.getWorkingDataTypeOfDataTypeString(hint)!=null) {
      var convertedValues = List[NodeTrait]()
      for(node <- values) {
        var convertedVal: NodeTrait = null
        try {
          convertedVal = Node.createTypedLiteral(HelperFunctions.convertValueToDataType(node.value, hint), hint, node.graph)
        } catch {
          case e: Exception => throw new R2RException("Error: Could not parse value: " + node.value + " to datatype: " + hint)
        }
        convertedValues = convertedVal :: convertedValues
      }
      values = convertedValues
    }else
      values = for(node <- values) yield Node.createTypedLiteral(node.value, hint, node.graph)
    values
  }

  private def getPredicate(tripleElement: TripleElement): NodeTrait = {
    return Node.createUriNode(tripleElement.getValue(0), "")
  }

  private def getSubjects(tripleElement: TripleElement, results: LDIFVariableResults): Iterable[NodeTrait] = {
    var subjects = List[NodeTrait]()
    val elemType = tripleElement.getType

    elemType match {
      case Type.IRI => subjects = Node.createUriNode(tripleElement.getValue(0), DEFAULT_GRAPH) :: subjects
      case Type.BLANKNODE => subjects = results.getBlankNode(tripleElement.getValue(0), DEFAULT_GRAPH) :: subjects
      case Type.VARIABLE => {
        val varName = tripleElement.getValue(0)
        val nodes = results.getResults(varName)
        for(node <- nodes.get)
          if(node.isResource)
            subjects = node :: subjects
          else
            subjects = Node.createUriNode(node.value, node.graph) :: subjects
      }
      case Type.IRIVARIABLE => {
        val varName = tripleElement.getValue(0)
        for(uri <- results.getLexicalResults(varName))
          subjects = Node.createUriNode(uri, DEFAULT_GRAPH) :: subjects
      }
    }
    subjects
  }

  override def getHints = targetPattern.getHints

  override def getClasses = targetPattern.getClasses

  override def getPath = targetPattern.getPath

  override def getProperties = targetPattern.getProperties

  override def getVariableDependencies = targetPattern.getVariableDependencies
}

object LDIFTargetPattern {
  def apply(targetPattern: TargetPattern): LDIFTargetPattern = {
    new LDIFTargetPattern(targetPattern)
  }
}