/* 
 * Copyright 2011 Freie Universität Berlin and MediaEvent Services GmbH & Co. K 
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

package ldif.util

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 30.06.11
 * Time: 15:11
 * To change this template use File | Settings | File Templates.
 */

import ldif.entity._
import ldif.entity.Restriction._
import collection.mutable.{ArrayBuffer, ListBuffer}

class EntityDescriptionToSparqlConverter {
  var entityGraphVars: Array[ArrayBuffer[String]] = null
  var useGraph : Boolean = false
  var graphName : String = null

  private def init(entityDesc: EntityDescription) {
    val nrOfQueries = math.max(entityDesc.patterns.size, 1)
    entityGraphVars = new Array[ArrayBuffer[String]](nrOfQueries)
    for(i <- 0 to (nrOfQueries-1))
      entityGraphVars(i) = new ArrayBuffer[String]
  }

  private def convert(entityDesc: EntityDescription, useGraph : Boolean, graphName : String = null): Seq[(String, Seq[String])] = {
    init(entityDesc)
    this.useGraph = useGraph
    this.graphName = graphName

    val varMaker = new VariableMaker("?ldifph")

    val restriction = convertRestriction(entityDesc.restriction, varMaker.getNextVar)

    val whereStringPatterns = createWhereStrings(entityDesc.patterns, varMaker.getNextVar)

    // This has to come last because of the entity graph
    val selectStrings = createSelectStrings(entityDesc)

    return assembleSparqlQueries(selectStrings, restriction, whereStringPatterns)
  }

  // Build the SPARQL queries out of its constituent parts
  private def assembleSparqlQueries(selects: Seq[String], restriction: String, wherePatterns: Seq[String]): Seq[(String, Seq[String])] = {
    val sparqlQueries = new ListBuffer[(String, Seq[String])]
    var index = 0
    for((select, where) <- selects.zip(wherePatterns)) {
      val querySB = new StringBuilder
      querySB.append(select)
      if (useGraph) {
        if (graphName != null)
          querySB.append(" FROM <"+ graphName +"> ")
        querySB.append(" WHERE ")
      }
      querySB.append(" { ").append(restriction).append( where).append(" } ORDER BY ?SUBJ")
      sparqlQueries.append((querySB.toString, entityGraphVars(index).map(_.substring(1))))
      index += 1
    }
    return sparqlQueries
  }

  private def createWhereStrings(patterns: IndexedSeq[IndexedSeq[Path]], resourceFunction: () => String): Seq[String] = {
    for(index <- 0 to (patterns.size-1)) yield createWhereString(index to index, EntityDescriptionToSparqlConverter.entityVar, convertPathToIndexedPath(patterns(index)), resourceFunction)
  }

  private def convertPathToIndexedPath(pattern: IndexedSeq[Path]): IndexedSeq[IndexedPath] = {
    val counter = new Counter
    for(path <- pattern) yield IndexedPath(path, counter.next())
  }

  private def createWhereString(range: Range, resource: String, pattern: IndexedSeq[IndexedPath], resourceFunction: () => String): String = {
    val whereSB = new StringBuilder

    val endPaths = pattern.filter(path => pathLength(path.path)==1)
    val ongoingPaths = pattern.filter(path => pathLength(path.path)>1)

    whereSB.append(createEndWhereStrings(resource, endPaths, resourceFunction, range))

    val partitions = partitionOngoingPaths(ongoingPaths)

    for(partition <- partitions) {
      val operator = partition._1
      val indexedPaths = partition._2
      val nextResource = resourceFunction()

      whereSB.append(createNamedGraphedTripleOutOfOperator(resource, operator, nextResource, resourceFunction, range))
      whereSB.append(createWhereString(range, nextResource, tailOfIndexedPaths(indexedPaths), resourceFunction))
    }

    return whereSB.toString
  }

  private def createNamedGraphedTripleOutOfOperator(resource: String, operator: PathOperator, nextResource: String, resourceFunction: () => String, patternRange: Range): String = {
    val graphVar = resourceFunction()
    checkForEntityGraph(resource, graphVar, patternRange)
    val namedGraphedTriple = new StringBuilder
    if (useGraph)
      namedGraphedTriple.append(createTripleOutOfOperator(resource, operator, nextResource)).toString
    else namedGraphedTriple.append("GRAPH ").append(graphVar).append(" { ").append(createTripleOutOfOperator(resource, operator, nextResource)).append(" } ").toString
  }

  // partition ongoing paths by their operator
  private def partitionOngoingPaths(onGoingPaths: IndexedSeq[IndexedPath]): Seq[Pair[PathOperator,IndexedSeq[IndexedPath]]] = {
    val partitions = new ListBuffer[Pair[PathOperator,IndexedSeq[IndexedPath]]]
    var workingSet = onGoingPaths
    while(!workingSet.isEmpty) {
      val nextOperator = workingSet.head.path.operators.head
      val partition = workingSet.filter(path => path.path.operators.head==nextOperator)
      workingSet = workingSet.filter(path => path.path.operators.head!=nextOperator)
      partitions.append(Pair(nextOperator,partition))
    }
    return partitions
  }

  private def tailOfIndexedPath(indexedPath: IndexedPath): IndexedPath = {
    return IndexedPath(Path(indexedPath.path.variable, indexedPath.path.operators.tail), indexedPath.index)
  }

  private def tailOfIndexedPaths(indexedPaths: IndexedSeq[IndexedPath]): IndexedSeq[IndexedPath] = {
    return for(iPath <- indexedPaths) yield tailOfIndexedPath(iPath)
  }

  private def createEndWhereStrings(resource: String, pattern: IndexedSeq[IndexedPath], resourceFunction: () => String, range: Range): String = {
    val whereParts = new StringBuilder
    for(path <- pattern) {
      val varName = VariableMaker.makeVar(path)
      if (!useGraph) {
        val graphName = varName + "graph"
        checkForEntityGraph(resource, graphName, range)
        whereParts.append("GRAPH ").append(graphName).append(" { ")
      }
      whereParts.append(createTripleOutOfOperator(resource, path.path.operators.head, varName))
      if (!useGraph)  whereParts.append(" } ")
    }
    return whereParts.toString()
  }

  private def checkForEntityGraph(resource: String, graphVar: String, range: Range) {
    for(i <- range) {
      if(resource==EntityDescriptionToSparqlConverter.entityVar) {
        entityGraphVars(i).append(graphVar)
      }
    }
  }

  private def createTripleOutOfOperator(resource: String, operator: PathOperator, nextResource: String): String = {
    val triple = new StringBuilder

    operator match { //TODO: Match other operators
        case ForwardOperator(uri) => triple.append(resource).append(" <").append(uri).append("> ").append(nextResource).append(" . ")
        case BackwardOperator(uri) => triple.append(nextResource).append(" <").append(uri).append("> ").append(resource).append(" . ")
        case _ => throw new UnsupportedOperationException("Path operator " + operator + "is not implemented, yet")
      }

    return triple.toString()
  }

  private def pathLength(path:Path): Int = {
    return path.operators.length
  }

  // Create the SELECT strings of the resulting SPARQL queries
  private def createSelectStrings(entityDesc: EntityDescription): Seq[String] = {
    val selectStrings = new ListBuffer[String]

    for(index <- 0 to (entityDesc.patterns.size-1))
      selectStrings.append(createSelectString(index, convertPathToIndexedPath(entityDesc.patterns(index))).toString())
    return selectStrings
  }

  private def createSelectString(index: Int, pattern: IndexedSeq[IndexedPath]): StringBuilder = {
    val sb = new StringBuilder
    sb.append("SELECT ") //TODO: Have maybe two versions
    sb.append(EntityDescriptionToSparqlConverter.afterSelect)
    for(path <- pattern) {
      sb.append("?").append(EntityDescriptionToSparqlConverter.resultVarBaseName).append(path.index).append(" ")
      if (!useGraph) sb.append("?").append(EntityDescriptionToSparqlConverter.resultVarBaseName).append(path.index).append("graph ")
    }
    sb.append(EntityDescriptionToSparqlConverter.entityVar).append(" ")
    if (!useGraph) {
      for(graphVar <- entityGraphVars(index))
        if(!graphVar.startsWith("?" + EntityDescriptionToSparqlConverter.resultVarBaseName))
          sb.append(graphVar).append(" ")
      }
    return sb
    }

  // return SPARQL representation of LDIF Restriction
  private def convertRestriction(restriction: Restriction, resourceFunction:() => String): String = {
    restriction.operator match {
      case None => return ""
      case Some(operator) => {
        return processOperator(operator, resourceFunction)
      }
    }
  }

  // return SPARQL string representation of restriction operator
  private def processOperator(operator: Operator, resourceFunction:() => String): String = {
    operator match { //TODO: Match other operators
      case And(children) => {
        val andBuilder = new StringBuilder
        andBuilder.append(" { ")
        for(child <- children)
          andBuilder.append(processOperator(child, resourceFunction))
        andBuilder.append(" } ")
        return andBuilder.toString()
      }
      case Condition(path, values) => {
         return processCondition(path, values, resourceFunction)
      }
      case Or(children) => {
        val orBuilder = new StringBuilder
        orBuilder.append(" { ")
        val internalStrings = for(child <- children) yield processOperator(child, resourceFunction)
        val unionString = internalStrings.reduceLeft(_ + " } UNION { " + _)
        orBuilder.append(unionString).append(" } ")
        return orBuilder.toString
      }
      case _ => throw new UnsupportedOperationException("Restriction operator " + operator + "is not implemented, yet")
    }
  }

  private def processCondition(path: Path, values: Set[Node], resourceFunction:() => String): String = {
    return processConditionPath(EntityDescriptionToSparqlConverter.entityVar, path.operators, values, resourceFunction)
  }

  // return the SPARQL string representation of a Condition path
  private def processConditionPath(resource: String, path: List[PathOperator], values: Set[Node], resourceFunction: () => String): String = {
    path match {
      case operator::Nil => {
        val valueSetB = new StringBuilder
        for(node <- values)
          valueSetB.append(createNamedGraphedTripleOutOfOperator(resource, operator, node.toNTriplesFormat, resourceFunction, 0 to (entityGraphVars.size-1)))

        return valueSetB.toString
      }
      case operator::rest => {
        val pathSB = new StringBuilder
        val nextResource = resourceFunction()
        pathSB.append(createNamedGraphedTripleOutOfOperator(resource, operator, nextResource, resourceFunction, 0 to (entityGraphVars.size-1)))
        pathSB.append(processConditionPath(nextResource, rest, values, resourceFunction))

        return pathSB.toString
      }
      case Nil => return ""
    }
  }
}

object EntityDescriptionToSparqlConverter {
  val entityVar = "?SUBJ"
  val resultVarBaseName = "ldifvar"
  val afterSelect = ""

  /**
   * Converts Entity Description into one or more SPARQL queries
   * @returns a pair of a SPARQL pattern and a sequence of variable names of the entity's graphs (can be multiple)
   */
  def convert(entityDesc: EntityDescription): Seq[(String, Seq[String])] = {
    (new EntityDescriptionToSparqlConverter).convert(entityDesc, false)
  }

  def convert(entityDesc: EntityDescription, useGraph : Boolean): Seq[(String, Seq[String])] = {
    (new EntityDescriptionToSparqlConverter).convert(entityDesc, useGraph)
  }

  def convert(entityDesc: EntityDescription, graphName : String): Seq[(String, Seq[String])] = {
    (new EntityDescriptionToSparqlConverter).convert(entityDesc, true, graphName)
  }
}

class VariableMaker(varPrefix: String) {
  var counter = 0

  def getNextVar(): String = {
    counter += 1
    return varPrefix + counter
  }
}

object VariableMaker {
  def makeVar(path: IndexedPath): String = {
    return "?ldifvar" + path.index
  }
}


class Counter {
  var counter = 0

  def next(): Int = {
    counter += 1
    return counter
  }
}