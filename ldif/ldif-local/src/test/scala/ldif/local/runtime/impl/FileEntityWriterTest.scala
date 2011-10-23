package ldif.local.runtime.impl

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import ldif.util.MemoryUsage
import ldif.entity.{EntityLocal, Node}
import java.io.File

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 10/23/11
 * Time: 7:44 PM
 * To change this template use File | Settings | File Templates.
 */

@RunWith(classOf[JUnitRunner])
class FileEntityWriterTest extends FlatSpec with ShouldMatchers {
  it should "keep the memory foot print constant" in {
    val tmpFile = File.createTempFile("ldif-test", "test")
    tmpFile.deleteOnExit
    val writer = new FileEntityWriter(null, tmpFile)
    for(i <- 1 to 1000000) {
      writer.write(new EntityLocal(Node.createUriNode("http://" + i, ""), null))
    }
    val endUsage = MemoryUsage.getMemoryUsage()
    writer.finish
    assert(endUsage < 5)
  }
}