package ldif.hadoop.mappers

import ldif.hadoop.types.SameAsPairWritable
import ldif.datasources.dump.QuadParser
import org.apache.hadoop.mapred.lib.MultipleOutputs
import org.apache.hadoop.io.{NullWritable, Text, LongWritable}
import org.apache.hadoop.mapred._

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 11/29/11
 * Time: 12:44 PM
 * To change this template use File | Settings | File Templates.
 */

class UpdateEntityLinksMapper extends MapReduceBase with Mapper[NullWritable, SameAsPairWritable, Text, SameAsPairWritable] {
  private var mos: MultipleOutputs = null

  override def configure(conf: JobConf) {
    mos = new MultipleOutputs(conf)
  }

  override def close() {
    mos.close()
  }

  override def map(nothing: NullWritable, sameAsPair: SameAsPairWritable, output: OutputCollector[Text, SameAsPairWritable], reporter: Reporter) {
    sameAsPair.isInLink = false
    output.collect(new Text(sameAsPair.from), sameAsPair)
    sameAsPair.isInLink = true
    output.collect(new Text(sameAsPair.to), sameAsPair)
  }
}