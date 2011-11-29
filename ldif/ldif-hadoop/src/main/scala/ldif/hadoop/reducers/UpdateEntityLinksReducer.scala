package ldif.hadoop.reducers

import ldif.hadoop.types.SameAsPairWritable
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.mapred.lib.MultipleOutputs
import org.apache.hadoop.mapred._
import collection.mutable.HashSet
import org.apache.hadoop.io.{WritableUtils, NullWritable, Text}
import ldif.hadoop.utils.URITranslatorHelperMethods
import java.util.Iterator

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 11/29/11
 * Time: 12:50 PM
 * To change this template use File | Settings | File Templates.
 */

class UpdateEntityLinksReducer extends MapReduceBase with Reducer[Text, SameAsPairWritable, NullWritable, SameAsPairWritable] {
  private var config: Configuration = null
  private var mos: MultipleOutputs = null

  override def configure(conf: JobConf) {
    config = conf
    mos = new MultipleOutputs(conf)
  }

  override def close() {
    mos.close()
  }

  override def reduce(entity: Text, sameAsPairs: Iterator[SameAsPairWritable], output: OutputCollector[NullWritable, SameAsPairWritable], reporter: Reporter) {
    var maxTarget = entity.toString

  }
}