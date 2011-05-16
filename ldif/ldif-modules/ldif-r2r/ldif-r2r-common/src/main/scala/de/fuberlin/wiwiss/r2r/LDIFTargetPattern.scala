package de.fuberlin.wiwiss.r2r

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 27.04.11
 * Time: 17:39
 * To change this template use File | Settings | File Templates.
 */

import ldif.entity.{EntityDescription, FactumRow, Entity, Node}
import ldif.local.runtime.{Quad, QuadWriter}
import de.fuberlin.wiwiss.r2r.TripleElement.Type
import scala.collection.JavaConversions._
import de.fuberlin.wiwiss.r2r.functions.HelperFunctions

class LDIFTargetPattern(targetPattern: TargetPattern) extends TargetPattern(targetPattern.getPath) {

  def writeQuads(results: LDIFVariableResults, quadWriter: QuadWriter) {
    for(triple <- getPath) {
      val subjects = getSubjects(triple.getSubject, results)
      val predicate = getPredicate(triple.getVerb)
      val objects = getObjects(triple.getObject, results)
      for(s <- subjects)
        for(o <- objects) quadWriter.write(Quad(s.toString, predicate.toString, o.toString, "default"))
    }
  }

  private def getObjects(tripleElement: TripleElement, results: LDIFVariableResults): Iterable[Node] = {
    val elemType = tripleElement.getType
    val lexValue = tripleElement.getValue(0)
    val objects: List[Node] = elemType match {
      case Type.IRI=> List(Node.createUriNode(lexValue, ""))
      case Type.IRIVARIABLE => for(uri <- results.getLexicalResults(lexValue)) yield Node.createUriNode(uri, "")
      case Type.DATATYPEVARIABLE => for(value <- getDataTypeVariableValues(tripleElement,results))
                                        yield Node.createTypedLiteral(value.value, getHints.get(tripleElement.getValue(0)), "")
      case Type.DATATYPESTRING => List(Node.createTypedLiteral(lexValue, tripleElement.getValue(1), ""))
      case Type.STRING => List(Node.createLiteral(lexValue, ""))
      case Type.STRINGVARIABLE => results.getResults(tripleElement.getValue(0)).get
      case Type.LANGTAGVARIABLE => for(value <- results.getResults(tripleElement.getValue(0)).get)
                                      yield Node.createLanguageLiteral(value.value, tripleElement.getValue(1), "")
      case Type.LANGTAGSTRING => List(Node.createLanguageLiteral(lexValue, tripleElement.getValue(1), ""))
      case Type.VARIABLE => results.getResults(tripleElement.getValue(0)).get
      case Type.BLANKNODE => List(Node.createBlankNode(lexValue, ""))
      case _ => {
        val dataType = elemType match {
          case Type.BOOLEAN => "http://www.w3.org/2001/XMLSchema#boolean"
          case Type.DECIMAL => "http://www.w3.org/2001/XMLSchema#decimal"
          case Type.INTEGER => "http://www.w3.org/2001/XMLSchema#integer"
          case Type.DOUBLE => "http://www.w3.org/2001/XMLSchema#double"
        }
        List(Node.createTypedLiteral(lexValue, dataType, ""))
      }
    }
    objects
  }

  private def getDataTypeVariableValues(element: TripleElement, results: LDIFVariableResults): List[Node] = {
    val varName = element.getValue(0)
    var values = results.getResults(varName).get
    val hint = getHints.get(varName)

    if(HelperFunctions.getWorkingDataTypeOfDataTypeString(hint)!=null) {
      var convertedValues = List[Node]()
      for(node <- values) {
        val convertedVal = Node.createTypedLiteral(HelperFunctions.convertValueToDataType(node.value, hint), hint, "")
        convertedValues = convertedVal :: convertedValues
      }
      values = convertedValues
    }
    values
  }

  private def getPredicate(tripleElement: TripleElement): Node = {
    return Node.createUriNode(tripleElement.getValue(0), "")
  }

  private def getSubjects(tripleElement: TripleElement, results: LDIFVariableResults): Iterable[Node] = {
    var subjects = List[Node]()
    val elemType = tripleElement.getType

    elemType match {
      case Type.IRI => subjects = Node.createUriNode(tripleElement.getValue(0), "") :: subjects
      case Type.BLANKNODE => subjects = Node.createBlankNode("_:BN","") :: subjects //TODO: Generate blank nodes
      case Type.VARIABLE => {
        val varName = tripleElement.getValue(0)
        val nodes = results.getResults(varName)
        for(node <- nodes.get) {
          if(node.isResource)
            subjects = node :: subjects
        }
      }
      case Type.IRIVARIABLE => {
        val varName = tripleElement.getValue(0)
        for(uri <- results.getLexicalResults(varName))
          subjects = Node.createUriNode(uri, "") :: subjects
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