package ldif.modules.sieve.fusion

/*
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

import functions.PassItOn
import sun.reflect.generics.reflectiveObjects.NotImplementedException
import ldif.entity.EntityDescription
import ldif.util.Prefixes

/**
 *
 * fusionSpecs: contains one spec per <Class> tag in the configuration.
 *             Each spec describes how to process one entity description (restricted to Class, with a pattern for each Property)
 *             Each spec holds a list of corresponding FusionFuctions for each property
 *
 * @author Pablo Mendes
 * @author Hannes Muehleisen
 */
class FusionConfig(//val name: String,
                   //val description: String,
                   val fusionSpecs: IndexedSeq[FusionSpecification],
                   val entityDescriptions: IndexedSeq[EntityDescription]) {

  def merge(c: FusionConfig): FusionConfig = {
    //TODO implement (to allow multiple configuration files)
    throw new NotImplementedException
  }

  override def equals(obj: Any) = {
    obj match {
      case ots: FusionConfig => fusionSpecs.equals(ots.fusionSpecs) && entityDescriptions.equals(ots.entityDescriptions)
      case _ => false
    }
  }

  override def toString(): String = {
    "FusionConfig, specs= " + fusionSpecs + " , ed=" + entityDescriptions
  }

}

object FusionConfig {
  def fromXML(node: scala.xml.Node)(implicit prefixes: Prefixes = Prefixes.empty) = {
    val augmentedPrefixes = (prefixes++Prefixes.stdPrefixes)
    val specs = (node \ "Class").map(FusionSpecification.fromXML(_)(augmentedPrefixes))
    val ed = (node \ "Class").map(FusionEntityDescription.fromXML(_)(augmentedPrefixes))
    new FusionConfig(specs.toIndexedSeq, ed.toIndexedSeq)
  }

  def empty: EmptyFusionConfig = {
    new EmptyFusionConfig
  }

}

/*
 This class should never be actually used for fusion. It simply signals that no config exists, and the framework should repeat the input.
 */
class EmptyFusionConfig extends FusionConfig(
  IndexedSeq(new FusionSpecification("EMPTY",IndexedSeq(new PassItOn), IndexedSeq("DEFAULT"))), IndexedSeq(EntityDescription.empty)
) {

}