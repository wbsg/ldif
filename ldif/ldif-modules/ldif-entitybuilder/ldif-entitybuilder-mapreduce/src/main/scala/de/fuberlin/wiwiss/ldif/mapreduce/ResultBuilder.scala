package de.fuberlin.wiwiss.ldif.mapreduce

import ldif.mapreduce.types.ValuePathWritable
import collection.mutable.HashMap
import ldif.entity._
import collection.mutable.{HashSet, ArrayBuffer}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 11/2/11
 * Time: 10:31 AM
 * To change this template use File | Settings | File Templates.
 */

class ResultBuilder(edmd: EntityDescriptionMetadata) {
  def checkRestriction(entityDescriptionID: Int, valuePaths: Seq[ValuePathWritable]): Boolean = {
    true //TODO: Implement
  }

  /**
   * Generate the result tables for all patterns (not restriction) for the entity description and the given value paths
   */
  def computeResultTables(entityDescriptionID: Int, valuePaths: Seq[ValuePathWritable]): IndexedSeq[Traversable[IndexedSeq[NodeWritable]]] = {
    val resultTables = new ArrayBuffer[Traversable[IndexedSeq[NodeWritable]]]()
    val nrOfPatterns = edmd.entityDescriptions(entityDescriptionID).patterns.length
    val valuePathsPerPattern = new Array[ArrayBuffer[ValuePathWritable]](nrOfPatterns)
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
    val result = joinValuePaths(0, pathIndexes, pathInfos, valuePaths)
    if(result.length==0)
      Traversable[IndexedSeq[NodeWritable]]()
    else
      result(0)._2
  }

  private def joinValuePaths(level: Int,  pathIndexes: Seq[Int], pathInfos: IndexedSeq[PathInfo], valuePaths: Seq[ValuePathWritable]): IndexedSeq[Pair[NodeWritable, Traversable[IndexedSeq[NodeWritable]]]] = {

    null
  }


  private def partitionValuePathsByPath(pattern: IndexedSeq[Path], valuePaths: ArrayBuffer[ValuePathWritable], edmd: EntityDescriptionMetadata): IndexedSeq[ArrayBuffer[ValuePathWritable]] = {
    val valuePathsPerPath = for (i <- 1 to pattern.length) yield new ArrayBuffer[ValuePathWritable]()
    for (valuePath <- valuePaths)
      valuePathsPerPath(edmd.pathMap(valuePath.pathID.get).pathIndex).append(valuePath)
    valuePathsPerPath
  }

  // Partition the paths by which property they follow (forward, backward)
  private def partitionPathIndexesByPathOperator(level: Int, pathIndexes: Seq[Int], pathInfos: IndexedSeq[PathInfo]): Seq[Seq[Int]] = {
    val pathOps = new HashSet[Pair[String, Boolean]]()
    for(index <- pathIndexes)
      pathOps.add(pathInfos(index).properties(level))
    val partitions = new HashMap[(String, Boolean), Seq[Int]]()
    for(pathOp <- pathOps)
      partitions.put(pathOp, new ArrayBuffer[Int]())
//    for(index <- )
    null //TODO
  }

  private def partitionValuePathsByNode(level: Int, pathIndexes: Seq[Int], propertyPathPerPath: IndexedSeq[PathInfo]): Seq[Seq[Int]] = {
    null//TODO
  }
}