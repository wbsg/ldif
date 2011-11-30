package ldif.hadoop.mappers

import ldif.entity.EntityWritable
import ldif.hadoop.types.{SameAsPairWritable, QuadWritable}
import ldif.datasources.dump.QuadParser
import ldif.util.Consts
import org.apache.hadoop.io._
import org.apache.hadoop.mapred.lib.MultipleOutputs
import org.apache.hadoop.mapred._
import ldif.hadoop.utils.{HadoopHelper, URITranslatorHelperMethods}
import ldif.runtime.Quad

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 11/24/11
 * Time: 5:24 PM
 * To change this template use File | Settings | File Templates.
 */

class ExtractSameAsPairsMapper extends MapReduceBase with Mapper[LongWritable, Text, Text, SameAsPairWritable] {
  private val parser = new QuadParser
  private var mos: MultipleOutputs = null

  override def configure(conf: JobConf) {
    mos = new MultipleOutputs(conf)
  }

  override def map(key: LongWritable, quadString: Text, output: OutputCollector[Text, SameAsPairWritable], reporter: Reporter) {
    var quad: Quad = null
    try {
      quad = parser.parseLine(quadString.toString)
    } catch {
      case e => quad = null
    }
    if(quad!=null && quad.predicate==Consts.SAMEAS_URI)
      URITranslatorHelperMethods.extractAndOutputSameAsPairs(quad.subject.value, quad.value.value, output, 1, mos.getCollector("debug", reporter).asInstanceOf[OutputCollector[Text, SameAsPairWritable]])
  }

  override def close() {
    mos.close()
  }
}
