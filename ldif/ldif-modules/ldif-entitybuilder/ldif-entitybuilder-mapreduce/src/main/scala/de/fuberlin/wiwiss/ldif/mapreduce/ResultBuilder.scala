package de.fuberlin.wiwiss.ldif.mapreduce

import ldif.mapreduce.types.ValuePathWritable
import collection.mutable.{HashMap, Map, ArrayBuffer, HashSet}
import ldif.entity._
import ldif.entity.Restriction._


/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 11/2/11
 * Time: 10:31 AM
 * To change this template use File | Settings | File Templates.
 */

class ResultBuilder(edmd: EntityDescriptionMetadata) {
  def checkRestriction(entityDescriptionID: Int, valuePaths: Seq[ValuePathWritable]): Boolean = {
    val restriction = edmd.entityDescriptions(entityDescriptionID).restriction
    if(restriction.operator == None)
      return true
    val restrictionPaths = filterRestrictionValuePaths(valuePaths)
    return checkRestrictionOperator(restriction.operator.get, restrictionPaths)
  }

  private def checkRestrictionOperator(operator: Operator, restrictionPaths: Seq[ValuePathWritable]): Boolean = {
    operator match {
      case Condition(path, values) => return checkCondition(path, values, restrictionPaths)
      case And(children) => return checkAnd(children, restrictionPaths)
      case Or(children) => return checkOr(children, restrictionPaths)
      case Not(operator) => return checkNot(operator, restrictionPaths)
      case Exists(path) => return checkExists(path, restrictionPaths)
    }
  }

  private def checkCondition(path: Path, nodes: Set[Node], restrictionPaths: Seq[ValuePathWritable]): Boolean = {
    val valuePathNodeSet = getValuesForPath(edmd.pathIdMap.get(path).get, restrictionPaths)
    for(node <- nodes if valuePathNodeSet.contains(node))
      return true
    false
  }

  private def getValuesForPath(pathId: Int, valuePaths: Seq[ValuePathWritable]): HashSet[NodeTrait] = {
    val nodeSet = new HashSet[NodeTrait]()
    for(valuePath <- valuePaths if valuePath.pathID.get==pathId)
      nodeSet.add(valuePath.values.get()(valuePath.length()).asInstanceOf[NodeTrait])
    nodeSet
  }

  private def checkAnd(operators: Traversable[Operator], restrictionPaths: Seq[ValuePathWritable]): Boolean = {
    for(operator <- operators if (!checkRestrictionOperator(operator, restrictionPaths)))
      return false
    true
  }

  private def checkOr(operators: Traversable[Operator], restrictionPaths: Seq[ValuePathWritable]): Boolean = {
    for(operator <- operators if (checkRestrictionOperator(operator, restrictionPaths)))
      return true
    false
  }

  private def checkNot(operator: Operator, restrictionPaths: Seq[ValuePathWritable]): Boolean = {
    !checkRestrictionOperator(operator, restrictionPaths)
  }

  private def checkExists(path: Path, restrictionPaths: Seq[ValuePathWritable]): Boolean = {
    val pathId = edmd.pathIdMap(path)
    for(valuePath <- restrictionPaths)
      if(valuePath.pathID.get==pathId)
        return true
    false
  }

  private def filterRestrictionValuePaths(valuePaths: Seq[ValuePathWritable]): Seq[ValuePathWritable] = {
    for(valuePath <- valuePaths if edmd.pathMap(valuePath.pathID.get).isRestrictionPath)
      yield valuePath
  }

  /**
   * Generate the result tables for all patterns (not restriction) for the entity description and the given value paths
   */
  def computeResultTables(entityDescriptionID: Int, valuePaths: Seq[ValuePathWritable]): IndexedSeq[Traversable[IndexedSeq[NodeWritable]]] = {
    val resultTables = new ArrayBuffer[Traversable[IndexedSeq[NodeWritable]]]()
    val nrOfPatterns = edmd.entityDescriptions(entityDescriptionID).patterns.length
    val valuePathsPerPattern = new Array[ArrayBuffer[ValuePathWritable]](nrOfPatterns)
    for(i <- 0 until valuePathsPerPattern.length)
      valuePathsPerPattern(i) = new ArrayBuffer[ValuePathWritable]()
    // Partition the value paths by pattern (and filter out restriction value paths)
    for(valuePath <- valuePaths if edmd.pathMap(valuePath.pathID.get).patternIndex >= 0)
      valuePathsPerPattern(edmd.pathMap(valuePath.pathID.get).patternIndex).append(valuePath)
    for(i <- 0 to (nrOfPatterns-1))
      resultTables.append(computeResultTable(entityDescriptionID, i, edmd, valuePathsPerPattern(i)))

    resultTables
  }

  def computeResultTable(entityDescriptionID: Int, patternIndex: Int, edmd: EntityDescriptionMetadata, valuePaths: ArrayBuffer[ValuePathWritable]): Traversable[IndexedSeq[NodeWritable]] = {
    val pattern = edmd.entityDescriptions(entityDescriptionID).patterns(patternIndex)
    val pathInfos = edmd.getPathInfosForPattern(entityDescriptionID, patternIndex)
    val pathsIndexes = (0 to (pattern.length-1)).toSeq
    return joinValuePaths(pathsIndexes, pathInfos, valuePaths)
  }

  private def joinValuePaths(pathIndexes: Seq[Int], pathInfos: IndexedSeq[PathInfo], valuePaths: Seq[ValuePathWritable]): Traversable[IndexedSeq[NodeWritable]] = {
    joinValuePaths(0, pathIndexes, pathInfos, valuePaths)
  }

  private def computePartialResultsForIndexPartitions(indexPartitions: Seq[Seq[Int]], nodePartition: Seq[ValuePathWritable], level: Int, pathInfos: IndexedSeq[PathInfo]): Seq[Seq[IndexedSeq[NodeWritable]]] = {
    val opPartitions = partitionValuePathsByIndexPartitions(indexPartitions, nodePartition)
    val partialResults = new ArrayBuffer[Seq[IndexedSeq[NodeWritable]]]()
    for ((indexPartition, index) <- indexPartitions.zipWithIndex) {
      // if there are no more joins for this partition (that is there is only one path in it) just return the values
      if (indexPartition.length == 1)
        partialResults.append(getPathValues(opPartitions(index)))
      // Else repeat the joining on the next level
      else
        partialResults.append(joinValuePaths(level + 1, indexPartition, pathInfos, opPartitions(index)))
    }
    partialResults
  }
  //TODO: remove index of path that ends on this level
  private def joinValuePaths(level: Int,  pathIndexes: Seq[Int], pathInfos: IndexedSeq[PathInfo], valuePaths: Seq[ValuePathWritable]): Seq[IndexedSeq[NodeWritable]] = {
    val results = new ArrayBuffer[IndexedSeq[NodeWritable]]()
    val addJoinNodeToResults = pathPointsToJoinNode(level, pathIndexes, pathInfos)
    val indexPartitions = partitionPathIndexesByPathOperator(level, pathIndexes, pathInfos)
    val nodePartitions = partitionValuePathsByNode(level, valuePaths)
    for((node, nodePartition) <- nodePartitions) {
      val partialResults = computePartialResultsForIndexPartitions(indexPartitions, nodePartition, level, pathInfos)
      results ++= calculatePartialResultTable(partialResults, node, addJoinNodeToResults)
    }
    results
  }

  private def pathPointsToJoinNode(level: Int, pathIndexes: Seq[Int], pathInfos: IndexedSeq[PathInfo]): Boolean = {
    var itDoes = false
    for(index <- pathIndexes if pathInfos(index).length==level)
      itDoes = true
    itDoes
  }

  private def calculatePartialResultTable(partialResults: Seq[Seq[IndexedSeq[NodeWritable]]], node: NodeWritable, addJoinNodeToResults: Boolean): Seq[IndexedSeq[NodeWritable]] = {
    val results = calculatePartialResultTable(partialResults)
    if(addJoinNodeToResults) {
      for(result <- results)
        yield node :: result.toList
    }
    results
  }

  private def calculatePartialResultTable(partialResults: Seq[Seq[IndexedSeq[NodeWritable]]]): Seq[IndexedSeq[NodeWritable]] = {
    partialResults.toList match {
      case head::Nil => head
      case head::tail => {
        val tailResults = calculatePartialResultTable(tail)
        for(headResult <- head; tailResult <- tailResults)
          yield addHeadResultToTailResult(headResult.toList, tailResult.toList).toIndexedSeq
      }
      case Nil => throw new RuntimeException("There always have to be partial results. Something's wrong.")
    }
  }

  private def addHeadResultToTailResult(headResult: List[NodeWritable], tailResult: List[NodeWritable]): List[NodeWritable] = {
    headResult match {
      case head::Nil => head :: tailResult
      case head::tail => head :: addHeadResultToTailResult (tail, tailResult)
      case Nil => tailResult
    }
  }

  private def getPathValues(valuePaths: Seq[ValuePathWritable]): Seq[IndexedSeq[NodeWritable]] = {
    Seq((for(valuePath <- valuePaths; values = valuePath.values.get())
      yield values(values.length-1).asInstanceOf[NodeWritable]).toIndexedSeq)
  }

  private def partitionValuePathsByIndexPartitions(indexPartitions: Seq[Seq[Int]], valuePaths: Seq[ValuePathWritable]): Seq[Seq[ValuePathWritable]] = {
    val result = new Array[Seq[ValuePathWritable]](indexPartitions.length)
    val indexToValuePathsMap = new HashMap[Int, ArrayBuffer[ValuePathWritable]]()
    for((indexPartition, index) <- indexPartitions.zipWithIndex) {
      val valuePaths = new ArrayBuffer[ValuePathWritable]()
      result(index) = valuePaths
      for(idx <- indexPartition)
        indexToValuePathsMap(idx) = valuePaths
    }
    for(valuePath <- valuePaths)
      indexToValuePathsMap(edmd.pathMap(valuePath.pathID.get).pathIndex).append(valuePath)
    result
  }

  // Partition the paths by which property they follow (and forward, backward type)
  private def partitionPathIndexesByPathOperator(level: Int, pathIndexes: Seq[Int], pathInfos: IndexedSeq[PathInfo]): Seq[Seq[Int]] = {
    val partitions = new HashMap[(String, Boolean), ArrayBuffer[Int]]()
    for(index <- pathIndexes)
      partitions.getOrElseUpdate(pathInfos(index).properties(level), new ArrayBuffer[Int]()).append(index)
    partitions.values.toSeq
  }

  private def partitionValuePathsByNode(level: Int, valuePaths: Seq[ValuePathWritable]): Map[NodeWritable, ArrayBuffer[ValuePathWritable]] = {
    val partitions = new HashMap[NodeWritable, ArrayBuffer[ValuePathWritable]]
    for(valuePath <- valuePaths)
      partitions.getOrElseUpdate(valuePath.values.get()(level).asInstanceOf[NodeWritable], new ArrayBuffer[ValuePathWritable]()).append(valuePath)
    partitions
  }
}