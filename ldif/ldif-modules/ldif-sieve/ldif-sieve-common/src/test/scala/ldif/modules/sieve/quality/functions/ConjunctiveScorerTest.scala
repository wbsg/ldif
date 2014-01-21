/*
 * LDIF
 *
 * Copyright 2011-2014 Universität Mannheim, MediaEvent Services GmbH & Co. KG
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

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import ldif.modules.sieve.quality.ScoringFunctionConjunctive
import ldif.entity.{NodeTrait, Node}


@RunWith(classOf[JUnitRunner])
class ConjunctiveScorerTest extends FlatSpec with ShouldMatchers {
  val scorer = new DummyScorer
  val subject = Node.fromString("<subject>")

  val one = new Node("1", "http://www.w3.org/2001/XMLSchema#float", Node.TypedLiteral, "graphId")
  val zero = new Node("0", "http://www.w3.org/2001/XMLSchema#float", Node.TypedLiteral, "graphId")
  val midway = new Node("0.5", "http://www.w3.org/2001/XMLSchema#float", Node.TypedLiteral, "graphId")
  val lower = new Node("0.2", "http://www.w3.org/2001/XMLSchema#float", Node.TypedLiteral, "graphId")
  val higher = new Node("0.7", "http://www.w3.org/2001/XMLSchema#float", Node.TypedLiteral, "graphId")

  it should "work on single values" in {
    (scorer.score(subject, Traversable(IndexedSeq(one))) should equal(1.0))
    (scorer.score(subject, Traversable(IndexedSeq(zero))) should equal(0.0))
  }

  it should "work on multiple 1/0 values" in {
    (scorer.score(subject, Traversable(IndexedSeq(one,one,one))) should equal(1.0))
    (scorer.score(subject, Traversable(IndexedSeq(one,one,one),IndexedSeq(one,one))) should equal(1.0))

    (scorer.score(subject, Traversable(IndexedSeq(zero,zero,zero))) should equal(0.0))
    (scorer.score(subject, Traversable(IndexedSeq(zero,zero,zero),IndexedSeq(zero,zero))) should equal(0.0))

    (scorer.score(subject, Traversable(IndexedSeq(zero,one,zero))) should equal(0.0))
    (scorer.score(subject, Traversable(IndexedSeq(zero,one,zero),IndexedSeq(zero,one))) should equal(0.0))
  }

  it should "minimum result for ambigous values such as 0.2" in {
    (scorer.score(subject, Traversable(IndexedSeq(one,midway,lower))) should equal(0.2))
  }
}

// dummy scorer which just uses the node value as result
class DummyScorer() extends ScoringFunctionConjunctive {
  def scoreSingleValue(node: NodeTrait) : Double =  {
    node.value.toDouble
  }
}