package ldif.local

import java.io.File
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import scheduler.{DataSource, RDFImportJob}

@RunWith(classOf[JUnitRunner])
class SchedulerTest extends FlatSpec with ShouldMatchers {

  val configFile = loadConfig("ldif/local/resources/config.xml")
  val scheduler = new Scheduler(LdifConfiguration.load(configFile))

  it should "schedule correctly" in {
    val url = "http://www.assembla.com/code/ldif/git/node/blob/ldif/ldif-singlemachine/src/test/resources/ldif/local/resources/sources/aba.nq.bz2"
    val job = new RDFImportJob(url,"weekly",new DataSource(null))
    scheduler.checkUpdate(job) should equal (true)
  }

  protected def loadConfig(config : String) =  {
    val configUrl = getClass.getClassLoader.getResource(config)
    new File(configUrl.toString.stripPrefix("file:"))
  }
}

