package ldif.hadoop.reducers

import ldif.hadoop.types.{SameAsPairWritable}
import scala.collection.mutable.HashSet
import java.util.Iterator
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.mapred._
import lib.MultipleOutputs
import org.apache.hadoop.io.{WritableUtils, NullWritable, Text}
import ldif.hadoop.utils.URITranslatorHelperMethods

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 11/28/11
 * Time: 5:13 PM
 * To change this template use File | Settings | File Templates.
 */

class JoinSameAsPairsReducer extends MapReduceBase with Reducer[Text, SameAsPairWritable, NullWritable, SameAsPairWritable] {
  private var config: Configuration = null
  private var mos: MultipleOutputs = null

  override def configure(conf: JobConf) {
    config = conf
    mos = new MultipleOutputs(conf)
  }

  override def reduce(joinEntity: Text, sameAsPairs : Iterator[SameAsPairWritable], output: OutputCollector[NullWritable, SameAsPairWritable], reporter: Reporter) {
    val inLinkSameAsPairs = new HashSet[SameAsPairWritable]()
    var toMax: String = null

    // Partition by in SameAsPairs
    // Also keep track of toMax
    while(sameAsPairs.hasNext) {
      val sameAsPair = sameAsPairs.next()
      if(sameAsPair.isInLink)
        inLinkSameAsPairs.add(WritableUtils.clone[SameAsPairWritable](sameAsPair,config))
      else {
        if(toMax==null || sameAsPair.to > toMax)
          toMax = sameAsPair.to
      }
    }

    val debugOutput = mos.getCollector("debug", reporter).asInstanceOf[OutputCollector[Text, SameAsPairWritable]]
    // Write out or join
    if(toMax==null)
      for(inSameAsPair <- inLinkSameAsPairs) {
        output.collect(NullWritable.get(), inSameAsPair)
        debugOutput.collect(new Text(""), inSameAsPair)
      }
    else
      for(inSameAsPair <- inLinkSameAsPairs)
        if(URITranslatorHelperMethods.simpleCompare(inSameAsPair.from, toMax)){
          inSameAsPair.to = toMax
          output.collect(NullWritable.get(), inSameAsPair)
          debugOutput.collect(new Text(""), inSameAsPair)
        }
  }

  override def close() {
    mos.close()
  }
}