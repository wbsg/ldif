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

class EntityDescriptionToSparqlConverter

object EntityDescriptionToSparqlConverter {
  val entityVar = "?SUBJ"

  def convert(entityDesc: EntityDescription): Seq[String] = {
    val varMaker = new VariableMaker("?ldifph")
    val bnMaker = new BlankNodeMaker()

    val selectStrings = createSelectStrings(entityDesc)

    val restriction = convertRestriction(entityDesc.restriction,bnMaker.getBlankNode)

    val paths = entityDesc.patterns

    val whereStringPatterns = createWhereStrings(entityDesc.patterns, varMaker.getNextVar)
    return assembleSparqlQueries(selectStrings, restriction, whereStringPatterns)
  }

  // Build the SPARQL queries out of its constituent parts
  private def assembleSparqlQueries(selects: Seq[String], restriction: String, wherePatterns: Seq[String]): Seq[String] = {
    val sparqlQueries = new ListBuffer[String]
    for((select, where) <- selects.zip(wherePatterns)) {
      val querySB = new StringBuilder
      querySB.append(select).append(" { ").append(restriction).append( where).append(" }")
      sparqlQueries.append(querySB.toString)
    }
    return sparqlQueries
  }

  private def createWhereStrings(patterns: IndexedSeq[IndexedSeq[Path]], resourceFunction: () => String): Seq[String] = {
    for(pattern <- patterns) yield createWhereString(entityVar, convertPathToIndexedPath(pattern), resourceFunction)
  }

  private def convertPathToIndexedPath(pattern: IndexedSeq[Path]): IndexedSeq[IndexedPath] = {
    val counter = new Counter
    for(path <- pattern) yield IndexedPath(path, counter.next())
  }

  private def createWhereString(resource: String, pattern: IndexedSeq[IndexedPath], resourceFunction: () => String): String = {
    val whereSB = new StringBuilder

    val endPaths = pattern.filter(path => pathLength(path.path)==1)
    val ongoingPaths = pattern.filter(path => pathLength(path.path)>1)

    whereSB.append(createEndWhereStrings(resource, endPaths, resourceFunction))

    val partitions = partitionOngoingPaths(ongoingPaths)

    for(partition <- partitions) {
      val operator = partition._1
      val indexedPaths = partition._2
      val nextResource = resourceFunction()

      whereSB.append(createTripleOutOfOperator(resource, operator, nextResource))
      whereSB.append(createWhereString(nextResource, tailOfIndexedPaths(indexedPaths), resourceFunction))
    }

    return whereSB.toString
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

  private def createEndWhereStrings(resource: String, pattern: IndexedSeq[IndexedPath], resourceFunction: () => String): String = {
    val whereParts = new StringBuilder
    for(path <- pattern) {
      val varName = VariableMaker.makeVar(path)
      whereParts.append("GRAPH ").append(varName).append("graph { ")
      whereParts.append(createTripleOutOfOperator(resource, path.path.operators.head, varName))
      whereParts.append(" } ")
    }
    return whereParts.toString()
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

    for(pattern <- entityDesc.patterns)
      selectStrings.append(createSelectString(convertPathToIndexedPath(pattern)).toString())
    return selectStrings
  }

  private def createSelectString(pattern: IndexedSeq[IndexedPath]): StringBuilder = {
    val sb = new StringBuilder
    sb.append("SELECT ")
    for(path <- pattern)
      sb.append("?ldifvar").append(path.index).append(" ?ldifvar").append(path.index).append("graph ")
    return sb.append("?SUBJ ")
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
      case _ => throw new UnsupportedOperationException("Restriction operator " + operator + "is not implemented, yet")
    }
  }

  private def processCondition(path: Path, values: Set[Node], resourceFunction:() => String): String = {
    return processConditionPath(entityVar, path.operators, values, resourceFunction)
  }

  // return the SPARQL string representation of a Condition path
  private def processConditionPath(resource: String, path: List[PathOperator], values: Set[Node], resourceFunction: () => String): String = {
    path match {
      case operator::Nil => {
        val valueSetB = new StringBuilder
        for(node <- values)
          valueSetB.append(createTripleOutOfOperator(resource, operator, node.toNTriplesFormat))

        return valueSetB.toString
      }
      case operator::rest => {
        val pathSB = new StringBuilder
        val nextResource = resourceFunction()
        pathSB.append(createTripleOutOfOperator(resource, operator, nextResource))
        pathSB.append(processConditionPath(nextResource, rest, values, resourceFunction))

        return pathSB.toString
      }
      case Nil => return ""
    }
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