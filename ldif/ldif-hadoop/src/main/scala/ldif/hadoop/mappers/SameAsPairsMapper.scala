package ldif.hadoop.mappers

import ldif.hadoop.types.SameAsPairWritable
import ldif.datasources.dump.QuadParser
import org.apache.hadoop.mapred.{Reporter, OutputCollector, Mapper, MapReduceBase}
import ldif.util.Consts
import ldif.hadoop.utils.URITranslatorHelperMethods
import org.apache.hadoop.io.{NullWritable, Text, LongWritable}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 11/28/11
 * Time: 5:06 PM
 * To change this template use File | Settings | File Templates.
 */

class SameAsPairsMapper extends MapReduceBase with Mapper[NullWritable, SameAsPairWritable, Text, SameAsPairWritable] {
  private val parser = new QuadParser
  private val sameAsPair = new SameAsPairWritable()
  private val uri = new Text()

  override def map(key: NullWritable, sameAsPair: SameAsPairWritable, output: OutputCollector[Text, SameAsPairWritable], reporter: Reporter) {
    val subj = sameAsPair.from
    val obj = sameAsPair.to
    URITranslatorHelperMethods.extractAndOutputSameAsPairs(subj, obj, output)
  }
}