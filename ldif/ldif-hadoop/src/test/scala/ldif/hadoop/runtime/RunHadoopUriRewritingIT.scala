package ldif.hadoop.runtime

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 1/3/12
 * Time: 12:30 PM
 * To change this template use File | Settings | File Templates.
 */

@RunWith(classOf[JUnitRunner])
class RunHadoopUriRewritingIT extends FlatSpec with ShouldMatchers{
  it should "rewrite all URIs correctly" in {
    val inputFile = getClass.getClassLoader.getResource("test/input.nt").getPath
    val sameAsFile = getClass.getClassLoader.getResource("test/sameAs.nt").getPath
    RunHadoopQuadConverter.execute(inputFile, "t/input")
    RunHadoopQuadConverter.execute(sameAsFile, "t/sameas")
    RunHadoopUriRewriting.execute("t/input", "t/sameas", "t/output_seq")
    HadoopQuadToTextConverter.execute("t/output_seq", "t/output")
  }
}