package ldif.hadoop.mappers

import ldif.hadoop.types.SameAsPairWritable
import org.apache.hadoop.io.{Text, NullWritable}
import org.apache.hadoop.mapred.lib.MultipleOutputs
import org.apache.hadoop.mapred._
import ldif.hadoop.utils.URITranslatorHelperMethods

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 11/30/11
 * Time: 12:57 PM
 * To change this template use File | Settings | File Templates.
 */

class WriteRemainingSameAsPairsMapper extends MapReduceBase with Mapper[NullWritable, SameAsPairWritable, Text, SameAsPairWritable] {
  override def map(key: NullWritable, sameAsPair: SameAsPairWritable, output: OutputCollector[Text, SameAsPairWritable], reporter: Reporter) {
    output.collect(new Text(sameAsPair.to), sameAsPair)
  }
}