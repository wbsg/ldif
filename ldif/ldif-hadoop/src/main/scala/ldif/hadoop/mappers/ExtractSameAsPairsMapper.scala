package ldif.hadoop.mappers

import ldif.entity.EntityWritable
import ldif.hadoop.types.{SameAsPairWritable, QuadWritable}
import org.apache.hadoop.mapred.{Reporter, OutputCollector, Mapper, MapReduceBase}
import ldif.datasources.dump.QuadParser
import ldif.util.Consts
import org.apache.hadoop.io._

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 11/24/11
 * Time: 5:24 PM
 * To change this template use File | Settings | File Templates.
 */

abstract class ExtractSameAsPairsMapper extends MapReduceBase with Mapper[LongWritable, Text, NullWritable, SameAsPairWritable] {
  private val parser = new QuadParser
  private val sameAsPair = new SameAsPairWritable()
  private val uri = new Text()

  def map(key: LongWritable, quadString: Text, output: OutputCollector[Text, SameAsPairWritable], reporter: Reporter) {
    val quad = parser.parseLine(quadString.toString)
    if(quad!=null && quad.predicate==Consts.SAMEAS_URI) {
      val from = quad.subject.value
      val to = quad.value.value
      sameAsPair.toBeExtended = false
      sameAsPair.from = from
      sameAsPair.to = to
      uri.set(to)
      output.collect(uri, sameAsPair)
      if(from < to) {
        uri.set(from)
        sameAsPair.toBeExtended=true
        output
      }

      else {
        sameAsPair.to = quad.subject.value
        sameAsPair.from = quad.value.value
      }
      sameAsPair.toBeExtended = false
      uri.set(from)
      output.collect(uri, sameAsPair)
      //if(sameAsPair.from < sameAsPair.to)

    }
  }

  private def setSameAsPair(sameAsPair: SameAsPairWritable, from: String, to: String, toBeExtended: Boolean) {
    sameAsPair.from = from
    sameAsPair.to = to
    sameAsPair.toBeExtended = toBeExtended
  }
}
