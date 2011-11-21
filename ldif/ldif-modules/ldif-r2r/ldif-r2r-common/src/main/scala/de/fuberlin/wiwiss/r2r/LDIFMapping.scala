/* 
 * LDIF
 *
 * Copyright 2011 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
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

import java.util.ArrayList
import scala.collection.JavaConversions._
import ldif.entity._
import ldif.local.runtime.QuadWriter
import collection.parallel.ParIterable
import ldif.util.Consts
import ldif.runtime.Quad

class LDIFMapping(val mapping: Mapping, val entityDescription: EntityDescription, variableToResultIndexMap: Map[String, Int]) {
  // Convert Target Patterns
  var targetPatterns = List[LDIFTargetPattern]()
  for(tp <- mapping.getTargetPatterns)
    targetPatterns = LDIFTargetPattern(tp) :: targetPatterns

  def executeMapping(entity: Entity, quadWriter: QuadWriter): Unit = {
    try {
      val results = entity.factums(0)
      for(row <- results) {
        val variableResults = getResults(row)
        variableResults.addVariableResult("SUBJ", entity.resource)
        executeAllFunctions(variableResults)
        executeTargetPatterns(variableResults, quadWriter)
      }
    } catch {
      case e: Exception => throw new R2RException("Error in executing mapping <" + mapping.getUri + ">", e)
    }
  }

  def executeMapping(entity: Entity): Traversable[Quad] = {
    try {
      val results = entity.factums(0)
      (for(row <- results) yield {
        val variableResults = getResults(row)
        variableResults.addVariableResult("SUBJ", entity.resource)
        executeAllFunctions(variableResults)
        executeTargetPatterns(variableResults)
      }).flatten
    } catch {
      case e: Exception => throw new R2RException("Error in executing mapping <" + mapping.getUri + ">", e)
    }
  }

  /**
   * A multi-tasking version of the executeMapping function
   */
  def executeMappingMT(entities: Iterable[Entity]): ParIterable[Quad] = {
    val parEntites = entities.par
    return parEntites.flatMap(entity => executeMapping(entity))
  }

  def executeTargetPatterns(results: LDIFVariableResults): Iterable[Quad] = {
    (for(targetPattern <- targetPatterns)
      yield targetPattern.createQuads(results)).flatten
  }

  def executeTargetPatterns(results: LDIFVariableResults, quadWriter: QuadWriter) {
    for(targetPattern <- targetPatterns)
      targetPattern.writeQuads(results, quadWriter)
  }

  def getResults(row: IndexedSeq[Node]): LDIFVariableResults = {
    val results = new LDIFVariableResults()
    for((variableName, index) <- variableToResultIndexMap)
      results.addVariableResult(variableName, row.get(index))
    results
  }

  def executeAllFunctions(results: LDIFVariableResults) {
    val dataTypeHints = mapping.getDatatypeHints
    for(function <- mapping.getFunctions) {
      val resultVarName = function.getVariableName
      var dtHint = dataTypeHints.get(resultVarName)
      if(dtHint==null) dtHint = ""

      val resultValues = executeFunction(function, results, dtHint)
      results.addVariableResult(resultVarName, resultValues)
    }
  }

  def executeFunction(functionExecution: FunctionExecution, results: LDIFVariableResults, dataTypeHint: String): List[Node] = {
    val arguments: java.util.List[Argument] = functionExecution.getArguments
    val realArguments = new ArrayList[java.util.List[String]]
    val function = functionExecution.getFunction
    var graph: String = Consts.DEFAULT_GRAPH

    //Gather arguments
    try {
      for(argument <- arguments) {
        if(argument.isInstanceOf[ConstantArgument])
          realArguments.add(List(argument.asInstanceOf[ConstantArgument].getValue))
        else if(argument.isInstanceOf[VariableArgument]) {
          val variableName = argument.asInstanceOf[VariableArgument].getVariableName
          realArguments.add(results.getLexicalResults(variableName))
          graph = results.getResults(variableName).get.head.graph
        }
        else if(argument.isInstanceOf[FunctionExecution]) {
          val nodeResults = executeFunction(argument.asInstanceOf[FunctionExecution], results, dataTypeHint)
          if(graph==Consts.DEFAULT_GRAPH)
            for(node <- nodeResults)
              if(node.graph!=Consts.DEFAULT_GRAPH)
                graph=node.graph
          val stringResults = for(node <- nodeResults) yield node.value
          realArguments.add(stringResults)
        }
      }

      (for(result <- function.execute(realArguments, dataTypeHint)) yield Node.createLiteral(result, graph)).toList
    } catch {
      case e => throw new FunctionExecutionException("Error in executing function <" + function.getURI + ">: " + e)
    }
  }

}

object LDIFMapping {
  def apply(mapping: Mapping): LDIFMapping = {
    val sourcePattern = mapping.getSourcePattern.getQueryBody
    val variableDependencies = new ArrayList[String]

    variableDependencies.addAll(mapping.computeQueryVariableDependencies)

    val (entityDesc, varToIndMap) = SourcePatternToEntityDescriptionTransformer.transform(sourcePattern, variableDependencies.toList, mapping.getPrefixMapper)
    new LDIFMapping(mapping, entityDesc, varToIndMap)
  }
}