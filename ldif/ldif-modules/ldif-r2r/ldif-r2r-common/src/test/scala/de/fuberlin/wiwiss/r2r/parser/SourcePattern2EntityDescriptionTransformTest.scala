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

  it should "parse values-paths" in {
    val sourcePattern = "?SUBJ <hasFriend> ?friend"
    transform(sourcePattern, List("friend"), prefixMapper).toString should equal ("(EntityDescription(Restriction(None),Vector(Vector(?SUBJ/<hasFriend>))),Map(friend -> 0))")
  }

  it should "create Restrictions from constant value paths" in {
    val sourcePattern = "?SUBJ a <someClass>"
    (transform(sourcePattern, List("friend"), prefixMapper).toString) should equal ("(EntityDescription(Restriction(Some(And(List(Condition(?SUBJ/<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>,Set(<someClass>)))))),Vector(Vector())),Map())")
  }
}