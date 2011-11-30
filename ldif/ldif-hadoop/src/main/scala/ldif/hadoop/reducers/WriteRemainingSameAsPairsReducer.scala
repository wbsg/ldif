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
 * Date: 11/30/11
 * Time: 12:59 PM
 * To change this template use File | Settings | File Templates.
 */

class WriteRemainingSameAsPairsReducer extends MapReduceBase with Reducer[Text, SameAsPairWritable, NullWritable, SameAsPairWritable] {
  private var config: Configuration = null
  private var mos: MultipleOutputs = null

  override def configure(conf: JobConf) {
    config = conf
    mos = new MultipleOutputs(conf)
  }

  override def reduce(clusterEntity: Text, sameAsPairs : Iterator[SameAsPairWritable], output: OutputCollector[NullWritable, SameAsPairWritable], reporter: Reporter) {
    val sameAsPairSet = new HashSet[SameAsPairWritable]()
    while(sameAsPairs.hasNext)
      sameAsPairSet.add(WritableUtils.clone[SameAsPairWritable](sameAsPairs.next(),config))
    val finishedOutput = mos.getCollector("finished", reporter).asInstanceOf[OutputCollector[NullWritable, SameAsPairWritable]]
    for(sameAsPair <- sameAsPairSet)
      finishedOutput.collect(NullWritable.get(), sameAsPair)
  }

  override def close() {
    mos.close()
  }
}