package ldif.modules.sieve.quality.functions

/*
 * Copyright 2011-2012 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
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

import org.slf4j.LoggerFactory
import ldif.entity.NodeTrait
import ldif.modules.sieve.quality.ScoringFunction
/**
 * Takes as input a date and outputs a real valued assessment of how close that date is to now, given a dateRange as normalizer.
 * The formula is:
 * 1 - ((today - lastUpdate) / dateRange)
 *
 * The parameter dateRange can be configured at instantiation time.
 *
 * Can be used to compute freshness of content (if applied to lastUpdate), or recency of content (if applied to creationDate).
 *
 * @author pablomendes
 */

class TimeCloseness(val dateRange: Int = 30) extends ScoringFunction {

  private val log = LoggerFactory.getLogger(getClass.getName)

  def score(metadataValues: Traversable[IndexedSeq[NodeTrait]]): Double = {
    metadataValues.toList
      .sortBy( node => { //in case of many "last" updates, sort and get the latest one.
        //check if node(0).datatype is dateTime  //TODO
        val date = node(0).value //TODO convert to date according to provided datatype format
      })
      .headOption match {
      case Some(node) => 1 //TODO compute 1 - ((today - lastUpdate) / dateRange)
      case None => 0
    }
  }

  def fromXML(node: scala.xml.Node) = {
    null //TODO implement
  }
}





