package de.fuberlin.wiwiss.r2r

import ldif.entity._
import ldif.local.runtime.QuadWriter
import java.util.ArrayList
import scala.collection.JavaConversions._

class LDIFMapping(mapping: Mapping, entityDescription: EntityDescription, variableToResultIndexMap: Map[String, Int]) {
  def executeMapping(entity: Entity, quadWriter: QuadWriter): Unit = {
    val results = entity.factums(0)
    for(row <- results) {
      val variableResults = getResults(row)
    }
  }

  def getResults(row: FactumRow): LDIFVariableResults = {
    val results = new LDIFVariableResults(row)
    for((variableName, index) <- variableToResultIndexMap)
      results.addVariableResult(variableName, row.get(index))
    results
  }

  def executeAllFunctions(results: LDIFVariableResults) {
    for(function <- mapping.getFunctions) {
      val resultVarName = function.getVariableName
//      val dtHint = function.
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