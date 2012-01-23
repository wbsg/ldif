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

package ldif.modules.sieve.fusion

import scala.util.matching.Regex
import org.slf4j.LoggerFactory
import ldif.entity.{NodeTrait, Node}
import ldif.modules.sieve.QualityAssessment

/**
 * example fusion function that keeps the first value
 * @author pablomendes
 */

class KeepFirst extends FusionFunction {

  private val log = LoggerFactory.getLogger(getClass.getName)

  override def fuse(values: Traversable[IndexedSeq[NodeTrait]], quality: QualityAssessment) : Traversable[IndexedSeq[NodeTrait]] = {
    if (values.nonEmpty) Seq(values.head) else Seq[IndexedSeq[NodeTrait]]()
  }

}

/**
 * Example fusion function that prefers values from certain Graphs given as input.
 * TODO expand to a list of graphs
 * @author pablomendes
 */

class TrustYourFriends(val preferredGraph: String) extends FusionFunction {

//  def this(regexString: String) = {
//    this(regexString.r)
//  }

  private val log = LoggerFactory.getLogger(getClass.getName)


  override def fuse(values: Traversable[IndexedSeq[NodeTrait]], quality: QualityAssessment) : Traversable[IndexedSeq[NodeTrait]] = {
    //todo sort according to desired quality indicator
    values.flatMap( n => { // for each property
      // get value for first property path
      val propertyValue = n(0) //TODO treat the case where the path is a tree (more than one property value)
      //log.info("factum:"+propertyValue);
      //log.info("graph:"+propertyValue.graph);
      if (propertyValue.graph matches preferredGraph) {
        Some(IndexedSeq(propertyValue)) //TODO treat the case where the path is a tree (more than one property value)
      } else {
        None
      }
    }).headOption match {
      case Some(node) => Seq(node)
      case None => Seq()
    }
  }

//  override def fuse(values: Traversable[IndexedSeq[NodeTrait]]) : Traversable[IndexedSeq[NodeTrait]] = {
//    var kept = IndexedSeq[NodeTrait]()
//    values.foreach( n => { // for each property
//      // get value for first property path
//      val propertyValue = n(0) //TODO treat the case where the path is a tree (more than one property value)
//      //log.info("factum:"+propertyValue);
//      //log.info("graph:"+propertyValue.graph);
//      if (propertyValue.graph matches preferredGraph)
//        kept = IndexedSeq(propertyValue)
//    })
//    Seq(kept)
//  }

}


class KeepUpToDate(val propertyName: String = "http://www4.wiwiss.fu-berlin.de/ldif/lastUpdate") extends FusionFunction {

  private val log = LoggerFactory.getLogger(getClass.getName)

  override def fuse(values: Traversable[IndexedSeq[NodeTrait]], quality: QualityAssessment) : Traversable[IndexedSeq[NodeTrait]] = {
    values.toList
      .sortBy( node => quality.score(propertyName, node(0).graph) ) //todo convert to dates
      .headOption match {
      case Some(node) => Seq(node)
      case None => Seq()
    }
  }

//  override def fuse(values: Traversable[IndexedSeq[NodeTrait]]) : Traversable[IndexedSeq[NodeTrait]] = {
//    var kept = IndexedSeq[NodeTrait]()
//    values.foreach( n => { // for each property
//      // get value for first property path
//      val propertyValue = n(0) //TODO treat the case where the path is a tree (more than one property value)
//      //log.info("factum:"+propertyValue);
//      //log.info("graph:"+propertyValue.graph);
//      if (propertyValue.graph matches preferredGraph)
//        kept = IndexedSeq(propertyValue)
//    })
//    Seq(kept)
//  }

}
