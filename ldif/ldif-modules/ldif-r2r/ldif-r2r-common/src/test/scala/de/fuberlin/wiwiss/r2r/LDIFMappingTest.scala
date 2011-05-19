package de.fuberlin.wiwiss.r2r

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 19.05.11
 * Time: 12:21
 * To change this template use File | Settings | File Templates.
 */

import org.scalatest.FlatSpec
import ldif.modules.r2r._
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import de.fuberlin.wiwiss.r2r._
import ldif.local.runtime.impl.{QuadQueue, EntityQueue}
import ldif.entity._
import collection.mutable.HashSet
import HelperFunctions._

@RunWith(classOf[JUnitRunner])
class LDIFMappingTest extends FlatSpec with ShouldMatchers {
  val repository = new Repository(new FileOrURISource("ldif/modules/r2r/testMapping.ttl"))

  it should "" in {
    (1) should equal (1)
  }
}