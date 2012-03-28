/*
 * LDIF
 *
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

package ldif.modules.sieve.quality.functions

import xml.Node
import ldif.modules.sieve.quality.ScoringFunction
import ldif.entity.{Entity, NodeTrait}

/**
 * Constructor of implementing classes should accept applicable Param and EnvironmentVariable values.
 * The values described in Input are passed at scoring time to the method "score".
 * @author pablomendes
 */

object RandomScoringFunction extends ScoringFunction {

  def fromXML(node: Node) = this

  def score(graphId: NodeTrait, metadataValues: Traversable[IndexedSeq[NodeTrait]]) = Math.random

}

