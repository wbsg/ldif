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
import org.scalatest.Spec
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

/**
 * Unit Test for the LDIFTargetPattern
 */

@RunWith(classOf[JUnitRunner])
class LDIFTargetPatternTest extends FunSuite {
  test("test") {
    assert(1===1)
  }
}
//  DefaultImplementations.register()
//
//  val executor = new SilkLocalExecutor()
//
//  "SilkLokalExecutor" should "return the correct entity descriptions" in
//  {
//    executor.input(task).entityDescriptions.head should equal (entityDescription)
//  }
//
//  private lazy val task =
//  {
//    val configStream = getClass.getClassLoader.getResourceAsStream("ldif/modules/silk/local/PharmGKB.xml")
//
//    val config = Configuration.load(configStream)
//
//    val module = new SilkModule(new SilkConfig(config))
//
//    module.tasks.head
//  }
//
//  private lazy val entityDescription =
//  {
//    implicit val prefixes = Prefixes(task.silkConfig.prefixes)
//
//    val stream = getClass.getClassLoader.getResourceAsStream("ldif/modules/silk/local/PharmGKB_EntityDescription.xml")
//
//    EntityDescription.fromXML(XML.load(stream))
//  }
