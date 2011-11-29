package ldif.hadoop.reducers

import ldif.hadoop.types.{SameAsPairWritable}
import scala.collection.mutable.HashSet
import java.util.Iterator
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.mapred._
import lib.MultipleOutputs
import ldif.hadoop.utils.URITranslatorHelperMethods
import org.apache.hadoop.io.{Text, WritableUtils, NullWritable}

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
    val sameAsPairSet = new HashSet[SameAsPairWritable]()
    var toMax: String = joinEntity.toString

    // Remove duplicates and find largest member of cluster
    while(sameAsPairs.hasNext) {
      val sameAsPair = sameAsPairs.next()
      sameAsPairSet.add(WritableUtils.clone[SameAsPairWritable](sameAsPair,config))
      if(URITranslatorHelperMethods.simpleCompare(toMax, sameAsPair.to))
        toMax = sameAsPair.to
    }

    val debugOutput = mos.getCollector("debug", reporter).asInstanceOf[OutputCollector[Text, SameAsPairWritable]]
    // Join and write out sameAsPairs if toMax > entity
    if(URITranslatorHelperMethods.simpleCompare(joinEntity.toString, toMax)) {
      output.collect(NullWritable.get(), new SameAsPairWritable(joinEntity.toString, toMax.toString))
      debugOutput.collect(new Text(""), new SameAsPairWritable(joinEntity.toString, toMax.toString))
      for(inSameAsPair <- sameAsPairSet)
        if(URITranslatorHelperMethods.simpleCompare(inSameAsPair.to, toMax)){
          inSameAsPair.from = inSameAsPair.to
          inSameAsPair.to = toMax
          output.collect(NullWritable.get(), inSameAsPair)
          debugOutput.collect(new Text(""), inSameAsPair)
        }
    }
  }

  override def close() {
    mos.close()
  }
}