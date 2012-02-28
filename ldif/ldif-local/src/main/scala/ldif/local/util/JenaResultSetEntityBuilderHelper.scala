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

   package ldif.local.util

import ldif.entity.{Node, EntityDescription}
import collection.mutable.ArrayBuffer
import com.hp.hpl.jena.query.{QuerySolution, ResultSet}
import scala.collection.JavaConversions._
import java.util.HashSet
   import com.hp.hpl.jena.rdf.model.{Resource, Literal, RDFNode}
   import ldif.util.{NTriplesStringConverter, EntityDescriptionToSparqlConverter}
   import ldif.local.runtime.{LocalNode, EntityWriter}
   import java.util.concurrent.atomic.AtomicInteger


   /*  There are two scenarios:
   *   1) ResultSets contain graph vars
   *   2) ResultSets does not contain graphs vars
   */

   //TODO refactor

object JenaResultSetEntityBuilderHelper {

  // (1)
  def buildEntitiesFromResultSet(resultSets: Seq[ResultSet], entityDescription: EntityDescription, entityWriter: EntityWriter, graphVars: Seq[Seq[String]], counter: AtomicInteger = null): Boolean = {
    val nrOfQueries = resultSets.size
    val resultManagers = for((resultSet, graphVar) <- resultSets zip graphVars) yield new ResultSetManager(resultSet, graphVar)
    var entityResults = for(rManager <- resultManagers) yield rManager.getNextEntityData

    while(entityResults.filter(_ != None).size > 0) {
      val entity = ResultSetManager.pickSmallestEntity(entityResults)
      val graph = getGraph(entity, entityResults)
      val factumTable = initFactumTable(nrOfQueries)
      assignResultsForEntity(entity, entityResults, factumTable)
      entityResults = updateEntityResults(entity, entityResults, resultManagers)

      entityWriter.write(EntityLocalComplete(entity, entityDescription, factumTable))
      if(counter!=null)
        counter.incrementAndGet()
    }

    entityWriter.finish
    true
  }

  // (2)
  def buildEntitiesFromResultSet(resultSets: Seq[ResultSet], entityDescription: EntityDescription, entityWriter: EntityWriter, graph: String): Boolean = {
    val nrOfQueries = resultSets.size
    val resultManagers = for((resultSet) <- resultSets) yield new ResultSetManager(resultSet, Nil)
    var entityResults = for(rManager <- resultManagers) yield rManager.getNextEntityData(graph)

    while(entityResults.filter(_ != None).size > 0) {
      val entity = ResultSetManager.pickSmallestEntity(entityResults)
      //val graph = getGraph(entity, entityResults)
      val factumTable = initFactumTable(nrOfQueries)
      assignResultsForEntity(entity, entityResults, factumTable)
      entityResults = updateEntityResults(entity, entityResults, resultManagers, graph)

      entityWriter.write(EntityLocalComplete(entity, entityDescription, factumTable))
    }

    entityWriter.finish
    true
  }

  private def assignResultsForEntity(entity: Node, entityResults: Seq[Option[EntityData]], factumTable: Array[Traversable[IndexedSeq[Node]]]) {
    for(i <- 0 to (entityResults.size-1))
      if(entityResults(i)!=None && entityResults(i).get.entity==entity)
        factumTable(i) = entityResults(i).get.factumTable
  }

  private def initFactumTable(size: Int): Array[Traversable[IndexedSeq[Node]]] = {
    val array = new Array[Traversable[IndexedSeq[Node]]](size)
    for(i <- 0 to (array.size-1))
      array(i) = new ArrayBuffer[IndexedSeq[Node]]
    array
  }

  private def getGraph(entity: Node, entityResults: Seq[Option[EntityData]]): String = {
    for(entityResult <- entityResults)
      if(entityResult!=None && entityResult.get.entity==entity)
        return entityResult.get.graphURI
    throw new RuntimeException("This should not happen ;)")
  }

  private def updateEntityResults(entity: Node, entityResults: Seq[Option[EntityData]], resultManagers: Seq[ResultSetManager], graph : String = null): Seq[Option[EntityData]] = {
    for((entityResult, resultManager) <- entityResults zip resultManagers) yield {
      if(entityResult!=None && entityResult.get.entity==entity)
        if (graph != null)
          // (2)
          resultManager.getNextEntityData(graph)
        else
          // (1)
          resultManager.getNextEntityData
      else
        entityResult
    }
  }

  def getFactumRow(entity: Node, entityGraph: String, resultSet: QuerySolution, nrOfSUBJGraphVars: Int): IndexedSeq[Node] = {
    val resultVarBaseName = EntityDescriptionToSparqlConverter.resultVarBaseName
    val row = new Row
    var nrOfVars: Int = 0
    for(v <- resultSet.varNames) if(v.startsWith(resultVarBaseName) &&  v.substring(resultVarBaseName.length).forall(Character.isDigit(_))) nrOfVars += 1

//    if(nrOfVars==0)
//      row.append(entity)
    for(index <- 1 to nrOfVars) {
      val node = resultSet.get(resultVarBaseName + index)
      val graph = resultSet.get(resultVarBaseName + index + "graph")
      row.append(convertNode(node, graph))
    }

    row
  }

  // (2)
  def getFactumRow(entity: Node, entityGraph: String, resultSet: QuerySolution): IndexedSeq[Node] = {
    val resultVarBaseName = EntityDescriptionToSparqlConverter.resultVarBaseName
    val row = new Row
    var nrOfVars: Int = 0
    for(v <- resultSet.varNames) if(v.startsWith(resultVarBaseName) &&  v.substring(resultVarBaseName.length).forall(Character.isDigit(_))) nrOfVars += 1

//    if(nrOfVars==0)
//      row.append(entity)
    for(index <- 1 to nrOfVars) {
      val node = resultSet.get(resultVarBaseName + index)
      row.append(convertNode(node, entityGraph))
    }

    row
  }

  private def convertLiteralNode(node: RDFNode, graphURI: String): Node = {
    val lexicalValue = node.asInstanceOf[Literal].getLexicalForm
    val datatype = node.asInstanceOf[Literal].getDatatypeURI
    val language = node.asInstanceOf[Literal].getLanguage

    if (datatype != null)
      return Node.createTypedLiteral(lexicalValue, datatype, graphURI)
    else if (language != "")
      return Node.createLanguageLiteral(lexicalValue, language, graphURI)
    else
      return Node.createLiteral(lexicalValue, graphURI)
  }

  def convertNode(node: RDFNode, graph: RDFNode): Node = {
    val graphURI = graph.asInstanceOf[Resource].getURI
    if(node.isURIResource) {
      return Node.createUriNode(node.asInstanceOf[Resource].getURI, graphURI)
    } else if(node.isLiteral) {
      return convertLiteralNode(node, graphURI)
    } else if(node.isAnon) {
      return Node.createBlankNode(node.asNode.getBlankNodeLabel, graphURI)
    } else
      throw new RuntimeException("Unknown node type for RDFNode: " + node) // Should never be the case

  }

  def convertNode(node: RDFNode, graphURI: String): Node = {
    if(node.isURIResource) {
      return Node.createUriNode(node.asInstanceOf[Resource].getURI, graphURI)
    } else if(node.isLiteral) {
      return convertLiteralNode(node, graphURI)
    } else if(node.isAnon) {
      return Node.createBlankNode(node.asInstanceOf[Resource].getId.getLabelString, graphURI)
    } else
      throw new RuntimeException("Unknown node type for RDFNode: " + node) // Should never be the case

  }

  class ResultSetManager(resultSet: ResultSet, graphvars : Seq[String]) {
    var entity: Node = null
    var entityGraph: String = null
    var factumTable = new HashSet[IndexedSeq[Node]]

    def getNextEntityData(): Option[EntityData] = {
      var returnEntityData: EntityData = null

      while(resultSet.hasNext) {
        val querySolution = resultSet.next
        val graph = getAGraph(querySolution)
        val subj = convertNode(querySolution.getResource("SUBJ"), graph)

        val factumRow = getFactumRow(subj, graph, querySolution, graphvars.size)
        if(entity!=subj && entity!=null) {
          returnEntityData = EntityData(entity, entityGraph, factumTable)
          factumTable = new HashSet[IndexedSeq[Node]]
        }
        entity=subj
        entityGraph=graph
        factumTable += factumRow
        if(returnEntityData != null) {
          return Some(returnEntityData)
        }
      }

      if(entity!=null) {
        if(factumTable.size>0) {
          returnEntityData = EntityData(entity, entityGraph, factumTable)
          factumTable = new HashSet[IndexedSeq[Node]]
          return Some(returnEntityData)
        }
        else
          return None
      } else
        return None
    }

    private def getAGraph(querySolution: QuerySolution): String = {
      for(graphVar <- graphvars) {
        if(querySolution.get(graphVar)!=null)
          return querySolution.get(graphVar).asInstanceOf[Resource].getURI
      }
      throw new RuntimeException("Cannot happen. There will always be at least one SUBJ graph.")
    }

    // (2)
    def getNextEntityData(graph: String): Option[EntityData] = {
      var returnEntityData: EntityData = null

      while(resultSet.hasNext) {
        val querySolution = resultSet.next
        val subj = convertNode(querySolution.getResource("SUBJ"), graph)

        val factumRow = getFactumRow(subj, graph, querySolution)
        if(entity!=subj && entity!=null) {
          returnEntityData = EntityData(entity, graph, factumTable)
          factumTable = new HashSet[IndexedSeq[Node]]
        }
        entity=subj
        factumTable += factumRow
        if(returnEntityData != null) {
          return Some(returnEntityData)
        }
      }

      if(entity!=null) {
        if(factumTable.size>0) {
          returnEntityData = EntityData(entity, graph, factumTable)
          factumTable = new HashSet[IndexedSeq[Node]]
          return Some(returnEntityData)
        }
        else
          return None
      } else
        return None
    }
  }

  object ResultSetManager {
    def pickSmallestEntity(dataOfEntities: Seq[Option[EntityData]]): Node = {
      var minEntity: Node = dataOfEntities.filter(_ != None).head.get.entity
      for(data <- dataOfEntities)
        if(data != None && (data.get.entity.compare(minEntity)<0))
          minEntity = data.get.entity

      return minEntity
    }
  }

  case class EntityData(val entity: Node, val graphURI: String, val factumTable: Traversable[IndexedSeq[Node]])

  class Row extends ArrayBuffer[Node] {
    override def hashCode : Int  = {
      var hash: Int = 1
      for (node <- this)
        hash = hash * 31 + node.hashCode
      hash
    }

    override def equals (other : Any) : Boolean = {
      if (this.asInstanceOf[AnyRef] eq other.asInstanceOf[AnyRef])
        true
      if (!(other.isInstanceOf[Row]))
        false
      else {
       val otherRow : Row = other.asInstanceOf[Row]
        if (this.size != otherRow.size)
          false
        else
        {
          var result = true
          for((node,i) <- this zipWithIndex)
            if (!node.equals(otherRow(i)))
              result = false
          result
        }
      }
    }

  }
}