package ldif.hadoop.mappers

import ldif.datasources.dump.QuadParser
import org.apache.hadoop.mapred.lib.MultipleOutputs
import org.apache.hadoop.mapred._
import ldif.runtime.Quad
import ldif.util.Consts
import ldif.hadoop.utils.URITranslatorHelperMethods
import ldif.hadoop.types.{QuadWritable, SameAsPairWritable}
import ldif.entity.NodeWritable
import org.apache.hadoop.io.{Text, NullWritable, LongWritable}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 12/5/11
 * Time: 5:00 PM
 * To change this template use File | Settings | File Templates.
 */

class ConvertTextToQuadsMapper extends MapReduceBase with Mapper[LongWritable, Text, NullWritable, QuadWritable] {
  private val parser = new QuadParser
  private var mos: MultipleOutputs = null

  override def configure(conf: JobConf) {
    mos = new MultipleOutputs(conf)
  }

  override def map(key: LongWritable, quadString: Text, output: OutputCollector[NullWritable, QuadWritable], reporter: Reporter) {
    var quad: Quad = null
    try {
      quad = parser.parseLine(quadString.toString)
    } catch {
      case e => quad = null
    }
    if(quad!=null)
      output.collect(NullWritable.get(), new QuadWritable(new NodeWritable(quad.subject), new Text(quad.predicate), new NodeWritable(quad.value), new Text(quad.graph)))
  }

  override def close() {
    mos.close()
  }
}
