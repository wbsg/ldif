package ldif.local.runtime.impl

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import ldif.local.datasources.dump.DumpExecutor
import ldif.datasources.dump.{DumpModule, DumpConfig}

@RunWith(classOf[JUnitRunner])
class DumpExecutorTest extends FlatSpec with ShouldMatchers {

  val sourceUrl = "http://www.assembla.com/code/ldif/git/node/blob/ldif/ldif-singlemachine/src/test/resources/ldif/local/resources/sources/aba.nq.bz2"

  val executor = new DumpExecutor
  val qq = new QuadQueue

     /* Disabled - remote test */
//  it should "load remote (bzip2 compress) file correctly" in {
//    executor.execute(task,null,qq)
//    qq.size should equal (1235)
//  }

  private lazy val task = {
    val config = new DumpConfig(Traversable(sourceUrl))
    val module = new DumpModule(config)
    module.tasks.head
  }

}