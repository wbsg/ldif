package de.fuberlin.wiwiss.r2r

import scala.collection.mutable.Map
import ldif.entity.{Node, FactumRow}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 10.05.11
 * Time: 14:57
 * To change this template use File | Settings | File Templates.
 */

class LDIFVariableResults(factumRow: FactumRow) {
	private val variableValues: Map[String, List[Node]] = Map()

	def addVariableResult(varName: String, results: List[Node]): Boolean = {
		if(!variableValues.contains(varName)) {
			variableValues.put(varName, results);
			return true;
		}
		return false;
	}

  def addVariableResult(varName: String, result: Node): Boolean = {
		if(!variableValues.contains(varName)) {
			variableValues.put(varName, List(result));
			return true;
		}
		return false;
	}

	def getResults(varName: String): Option[List[Node]] = {
		variableValues.get(varName)
	}

  def getLexicalResults(varName: String): List[String] = {
    for(node <- variableValues(varName)) yield node.value
  }
}