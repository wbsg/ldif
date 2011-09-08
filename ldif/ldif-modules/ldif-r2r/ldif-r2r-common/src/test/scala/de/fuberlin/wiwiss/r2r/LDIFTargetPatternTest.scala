package de.fuberlin.wiwiss.r2r

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 12.05.11
 * Time: 16:33
 * To change this template use File | Settings | File Templates.
 */


import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import ldif.entity._
import ldif.local.runtime.QuadWriter
import ldif.local.runtime.impl.QuadQueue
import ldif.runtime.Quad

/**
 * Unit Test for the LDIFTargetPattern
 */

@RunWith(classOf[JUnitRunner])
class LDIFTargetPatternTest extends FlatSpec with ShouldMatchers {
  behavior of "an LDIFTargetPattern"

  val quadWriter = new QuadQueue {
    var count = 0
    override def write(quad: Quad) {
      count = count + 1
    }
  }

  it should "generate quads out of LDIFVariableResults" in {
    val results = new LDIFVariableResults
    results.addVariableResult("SUBJ", Node.createUriNode("subjectTest1", ""))
    results.addVariableResult("o", Node.createLiteral("literalValueTest1", "someGraphTest1"))
    val targetPattern = TargetPattern.parseTargetPattern("?SUBJ <outputPropertyTest1> ?o", new PrefixMapper, new java.util.HashSet[String])
    val ldiftargetPattern = new LDIFTargetPattern(targetPattern)
    ldiftargetPattern.writeQuads(results, quadWriter)
    (quadWriter.count) should equal (1)
  }
}