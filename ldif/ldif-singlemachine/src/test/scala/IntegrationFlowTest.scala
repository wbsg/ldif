/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 10.06.11
 * Time: 17:44
 * To change this template use File | Settings | File Templates.
 */

import java.io.File
import org.scalatest.FlatSpec
import ldif.modules.r2r._
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import de.fuberlin.wiwiss.r2r._
import ldif.local.runtime.impl.{QuadQueue, EntityQueue}
import ldif.local.runtime.Quad
import ldif.entity._
import collection.mutable.HashSet

@RunWith(classOf[JUnitRunner])
class IntegrationFlowTest extends FlatSpec with ShouldMatchers {
  val configFile = new File("ldif/local/resources/config.xml")

  it should "run the whole integration flow correctly" in {
    true should equal (true)//TODO: implement test
  }
}