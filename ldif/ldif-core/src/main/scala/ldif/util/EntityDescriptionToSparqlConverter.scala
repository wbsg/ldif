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
import scala.collection.mutable.ListBuffer

class EntityDescriptionToSparqlConverter {
  var pickedEntityGraph: Array[Boolean] = null  // true if a graph for the entity has been picked
  var entityGraphVar: Array[String] = null

  private def init(entityDesc: EntityDescription) {
    val nrOfQueries = math.max(entityDesc.patterns.size, 1)
    pickedEntityGraph = new Array[Boolean](nrOfQueries)
    entityGraphVar = new Array[String](nrOfQueries)
    for(i <- 0 to (nrOfQueries-1))
      pickedEntityGraph(i) = false
  }

  private def convert(entityDesc: EntityDescription): Seq[(String, String)] = {
    init(entityDesc)
    val varMaker = new VariableMaker("?ldifph")

    val restriction = convertRestriction(entityDesc.restriction, varMaker.getNextVar)

    val whereStringPatterns = createWhereStrings(entityDesc.patterns, varMaker.getNextVar)

    // This has to come last because of the entity graph
    val selectStrings = createSelectStrings(entityDesc)

    return assembleSparqlQueries(selectStrings, restriction, whereStringPatterns)
  }

  // Build the SPARQL queries out of its constituent parts
  private def assembleSparqlQueries(selects: Seq[String], restriction: String, wherePatterns: Seq[String]): Seq[(String, String)] = {
    val sparqlQueries = new ListBuffer[(String, String)]
    var index = 0
    for((select, where) <- selects.zip(wherePatterns)) {
      val querySB = new StringBuilder
      querySB.append(select).append(" { ").append(restriction).append( where).append(" } ORDER BY ?SUBJ")
      sparqlQueries.append((querySB.toString, entityGraphVar(index).substring(1)))
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
    namedGraphedTriple.append("GRAPH ").append(graphVar).append(" { ").append(createTripleOutOfOperator(resource, operator, nextResource)).append(" } ").toString
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
      val graphName = varName + "graph"
      checkForEntityGraph(resource, graphName, range)
      whereParts.append("GRAPH ").append(graphName).append(" { ")
      whereParts.append(createTripleOutOfOperator(resource, path.path.operators.head, varName))
      whereParts.append(" } ")
    }
    return whereParts.toString()
  }

  private def checkForEntityGraph(resource: String, graphVar: String, range: Range) {
    for(i <- range) {
      if(!pickedEntityGraph(i) && resource==EntityDescriptionToSparqlConverter.entityVar) {
        pickedEntityGraph(i) = true
        entityGraphVar(i) = graphVar
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
    sb.append("SELECT ")
    for(path <- pattern)
      sb.append("?").append(EntityDescriptionToSparqlConverter.resultVarBaseName).append(path.index).append(" ?").append(EntityDescriptionToSparqlConverter.resultVarBaseName).append(path.index).append("graph ")
    sb.append(EntityDescriptionToSparqlConverter.entityVar).append(" ")
    if(!entityGraphVar(index).startsWith("?" + EntityDescriptionToSparqlConverter.resultVarBaseName))
      sb.append(entityGraphVar(index)).append(" ")
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
          valueSetB.append(createNamedGraphedTripleOutOfOperator(resource, operator, node.toNTriplesFormat, resourceFunction, 0 to (entityGraphVar.size-1)))

        return valueSetB.toString
      }
      case operator::rest => {
        val pathSB = new StringBuilder
        val nextResource = resourceFunction()
        pathSB.append(createNamedGraphedTripleOutOfOperator(resource, operator, nextResource, resourceFunction, 0 to (entityGraphVar.size-1)))
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

  /**
   * Converts Entity Description into one or more SPARQL queries
   * @returns a pair of a SPARQL pattern and the variable name of the entity's graph
   */
  def convert(entityDesc: EntityDescription): Seq[(String, String)] = {
    (new EntityDescriptionToSparqlConverter).convert(entityDesc)
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

class BlankNodeMaker() {
  var counter = 0

  def getBlankNode(): String = {
    counter += 1
    return "_:bn" + counter
  }
}

class Counter {
  var counter = 0

  def next(): Int = {
    counter += 1
    return counter
  }
}