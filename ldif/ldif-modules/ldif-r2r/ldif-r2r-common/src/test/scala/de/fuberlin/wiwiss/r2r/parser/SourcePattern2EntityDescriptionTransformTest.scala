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

package de.fuberlin.wiwiss.r2r.parser

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 12.05.11
 * Time: 18:48
 * To change this template use File | Settings | File Templates.
 */

import de.fuberlin.wiwiss.r2r.PrefixMapper
import org.scalatest.matchers.ShouldMatchers
import de.fuberlin.wiwiss.r2r.SourcePatternToEntityDescriptionTransformer._
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import ldif.entity._
import ldif.entity.Restriction._

@RunWith(classOf[JUnitRunner])
class SourcePattern2EntityDescriptionTransformTest extends FlatSpec with ShouldMatchers {
  behavior of "a Source Pattern to Entity Description parser"

  val prefixMapper = new PrefixMapper

  it should "convert complex mappings" in {
    val sourcePattern = "?nameResource <http://last> ?n . ?SUBJ <http://nameResource> ?nameResource .  ?nameResource <http://first> ?v ."
    transform(sourcePattern, Set("v", "n"), prefixMapper).toString should equal ("(EntityDescription(Restriction(None),Vector(Vector(?SUBJ/<http://nameResource>/<http://first>, ?SUBJ/<http://nameResource>/<http://last>))),Map(v -> 0, n -> 1))")
  }

  it should "parse values-paths" in {
    val sourcePattern = "?SUBJ <hasFriend> ?friend"
    transform(sourcePattern, Set("friend"), prefixMapper).toString should equal ("(EntityDescription(Restriction(None),Vector(Vector(?SUBJ/<hasFriend>))),Map(friend -> 0))")
  }

  it should "create Restrictions from constant value paths" in {
    val sourcePattern = "?SUBJ a <someClass>"
    transform(sourcePattern, Set("friend"), prefixMapper).toString should equal ("(EntityDescription(Restriction(Some(And(List(Condition(?SUBJ/<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>,Set(<someClass>)))))),Vector(Vector())),Map())")
  }
}