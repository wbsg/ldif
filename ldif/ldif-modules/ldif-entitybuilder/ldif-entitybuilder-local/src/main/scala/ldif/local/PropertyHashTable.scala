/*
* LDIF
*
* Copyright 2011-2013 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
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

package ldif.local

import collection.mutable.HashMap
import org.slf4j.LoggerFactory
import ldif.entity.{PathOperator, BackwardOperator, ForwardOperator, EntityDescription}
import ldif.entity.Restriction._

class PropertyHashTable(entityDescriptions: Seq[EntityDescription]) {
  private val hashtable = new HashMap[String, PropertyType.Value]
  private val log = LoggerFactory.getLogger(getClass.getName)
  private var allUriNodesNeeded = false

  buildPHT

  def areAllUriNodesNeeded = allUriNodesNeeded

  def contains(property: String) = hashtable.contains(property)

  def get(property: String) = hashtable.get(property)

  private def buildPHT {
     hashtable.clear
     val startTime = System.currentTimeMillis

     for (ed <- entityDescriptions){

       // analyse restriction
       addRestrictionProperties(ed.restriction.operator)

       // analyse patterns
       for (patterns <- ed.patterns)
         for (pattern <- patterns)
           for (op <- pattern.operators)
            op match {
               case op:ForwardOperator => updatePHT(op.property.toString, PropertyType.FORW)
               case op:BackwardOperator => updatePHT(op.property.toString, PropertyType.BACK)
               case _ =>
            }
     }

     log.info("Analyse Entity Descriptions took " + ((System.currentTimeMillis - startTime)) + " ms")
     //log.info(" [ PHT ] \n > keySet = ("+PHT.size.toString +") \n   - "+ PHT.mkString("\n   - "))
  }

    private def updatePHT(property :String, propertyType : PropertyType.Value ){
      hashtable.get(property) match {
        case Some(PropertyType.BOTH) =>
        case Some(PropertyType.FORW) =>
          if (propertyType==PropertyType.BACK)
            hashtable.put(property, PropertyType.BOTH)
          else hashtable.put(property, propertyType)
        case Some(PropertyType.BACK) =>
          if (propertyType==PropertyType.FORW)
            hashtable.put(property, PropertyType.BOTH)
          else hashtable.put(property, propertyType)
        case None => hashtable.put(property, propertyType)
      }
  }

  // Find all properties from a given operator and add those to the property hash table
  private def updatePHT(op: PathOperator, reverse: Boolean = true) {
    op match {
      case op: ForwardOperator => if(reverse) updatePHT(op.property.toString, PropertyType.BACK)
        else updatePHT(op.property.toString, PropertyType.FORW)
      case op: BackwardOperator => if(reverse) updatePHT(op.property.toString, PropertyType.FORW)
        else updatePHT(op.property.toString, PropertyType.BACK)
      case _ =>
    }
  }

  private def addRestrictionProperties(operator : Option[Operator]) {
    operator match {
      case Some(cond:Condition) =>
        for (op <- cond.path.operators)
          updatePHT(op)
      case Some(and:And) =>  {
        for (child <- and.children)
          addRestrictionProperties(Some(child))
      }
      case Some(or:Or) =>  {
        for (child <- or.children)
          addRestrictionProperties(Some(child))
      }
      case Some(not:Not) =>
        addRestrictionProperties(Some(not.op))
      case Some(exists:Exists) => {
        for (op <- exists.path.operators)
          updatePHT(op, false)
        allUriNodesNeeded = true
      }
      case None => {
        allUriNodesNeeded = true
      }
    }
  }
}

object PropertyType extends Enumeration {
  val FORW, BACK, BOTH = Value
}

