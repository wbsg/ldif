package de.fuberlin.wiwiss.r2r

import ldif.entity.{EntityDescription, FactumRow, Entity, Node}
import ldif.local.runtime.QuadWriter
import java.util.ArrayList
import scala.collection.JavaConversions._

class LDIFMapping(mapping: Mapping, entityDescription: EntityDescription, variableToResultIndexMap: Map[String, Int]) {
  // Convert Target Patterns
  var targetPatterns = List[LDIFTargetPattern]()
  for(tp <- mapping.getTargetPatterns)
    targetPatterns = LDIFTargetPattern(tp) :: targetPatterns

  def executeMapping(entity: Entity, quadWriter: QuadWriter): Unit = {
    val results = entity.factums(0)
    for(row <- results) {
      val variableResults = getResults(row)
      executeAllFunctions(variableResults)
      executeTargetPatterns(variableResults, quadWriter)
    }
  }

  def executeTargetPatterns(results: LDIFVariableResults, quadWriter: QuadWriter) {
    for(targetPattern <- targetPatterns)
      targetPattern.writeQuads(results, quadWriter)
  }

  def getResults(row: FactumRow): LDIFVariableResults = {
    val results = new LDIFVariableResults(row)
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
    val arguments = functionExecution.getArguments
    val realArguments = new ArrayList[java.util.List[String]]
    val function = functionExecution.getFunction

    //Gather arguments
    try {
      for(argument <- arguments) {
        if(argument.isInstanceOf[ConstantArgument])
          realArguments.add(List(argument.asInstanceOf[ConstantArgument].getValue))
        else if(argument.isInstanceOf[VariableArgument])
          realArguments.add(results.getLexicalResults(argument.asInstanceOf[VariableArgument].getVariableName))
        else if(argument.isInstanceOf[FunctionExecution]) {
          val stringResults = for(node <- executeFunction(argument.asInstanceOf[FunctionExecution], results, dataTypeHint)) yield node.value
          realArguments.add(stringResults)
        }
      }

      (for(result <- function.execute(realArguments, dataTypeHint)) yield Node.createLiteral(result, "default")).toList
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