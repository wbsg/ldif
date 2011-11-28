package ldif.hadoop.reducers

import ldif.entity.EntityWritable
import ldif.hadoop.types.{SameAsPairWritable, ValuePathWritable, EntityDescriptionNodeWritable}
import org.apache.hadoop.io.{NullWritable, Text, IntWritable}
import org.apache.hadoop.mapred.{Reporter, OutputCollector, Reducer, MapReduceBase}
import scala.collection.mutable.HashSet
import java.util.Iterator

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 11/28/11
 * Time: 5:13 PM
 * To change this template use File | Settings | File Templates.
 */

class JoinSameAsPairsReducer extends MapReduceBase with Reducer[Text, SameAsPairWritable, NullWritable, SameAsPairWritable] {
  override def reduce(joinEntity: Text, sameAsPairs : Iterator[SameAsPairWritable], output: OutputCollector[NullWritable, SameAsPairWritable], reporter: Reporter) {
    val inLinkSameAsPairs = new HashSet[SameAsPairWritable]()
    var toMax: String = null

    // Partition by in SameAsPairs
    // Also keep track of toMax
    while(sameAsPairs.hasNext) {
      val sameAsPair = sameAsPairs.next()
      if(sameAsPair.isInLink)
        inLinkSameAsPairs.add(sameAsPair)
      else {
        if(toMax==null || sameAsPair.to > toMax)
          toMax = sameAsPair.to
      }
    }

    // Write out or join (TODO: in case of no join => write out needed?)
    if(toMax==null)
      for(inSameAsPair <- inLinkSameAsPairs)
        output.collect(NullWritable.get(), inSameAsPair)
    else
      for(inSameAsPair <- inLinkSameAsPairs)
        if(inSameAsPair.from < toMax){
          inSameAsPair.to = toMax
          output.collect(NullWritable.get(), inSameAsPair)
        }
  }
}