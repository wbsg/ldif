package de.fuberlin.wiwiss.r2r

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 27.04.11
 * Time: 17:39
 * To change this template use File | Settings | File Templates.
 */

import ldif.entity.{EntityDescription, FactumRow, Entity, Node}
import ldif.local.runtime.QuadWriter
import de.fuberlin.wiwiss.r2r.TripleElement.Type
import scala.collection.JavaConversions._

class LDIFTargetPattern(path: java.util.List[Triple]) extends TargetPattern(path) {

  def writeQuads(results: LDIFVariableResults, quadWriter: QuadWriter) {
    for(triple <- path) {
      val subjects = getSubjects(triple.getSubject, results)
      val predicates = getPredicate(triple.getVerb)

    }
  }

  private def getObjects(tripleElement: TripleElement, results: LDIFVariableResults): Iterable[Node] = {
    val elemType = tripleElement.getType
    var objects = List[Node]()
    val lexValue = tripleElement.getValue(0)
    elemType match {
      case Type.IRI=> objects = Node.createUriNode(lexValue, "") :: objects
      case Type.IRIVARIABLE => objects = for(uri <- results.getLexicalResults(lexValue)) yield Node.createUriNode(uri, "")
    }
    objects
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
}

object LDIFTargetPattern {
  def apply(targetPattern: TargetPattern): LDIFTargetPattern = {
    new LDIFTargetPattern(targetPattern.getPath)
  }
}