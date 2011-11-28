package ldif.hadoop.mappers

import ldif.entity.EntityWritable
import ldif.hadoop.types.{SameAsPairWritable, QuadWritable}
import org.apache.hadoop.mapred.{Reporter, OutputCollector, Mapper, MapReduceBase}
import ldif.datasources.dump.QuadParser
import ldif.util.Consts
import org.apache.hadoop.io._
import ldif.hadoop.utils.URITranslatorHelperMethods

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 11/24/11
 * Time: 5:24 PM
 * To change this template use File | Settings | File Templates.
 */

class ExtractSameAsPairsMapper extends MapReduceBase with Mapper[LongWritable, Text, Text, SameAsPairWritable] {
  private val parser = new QuadParser
  private val sameAsPair = new SameAsPairWritable()
  private val uri = new Text()

  override def map(key: LongWritable, quadString: Text, output: OutputCollector[Text, SameAsPairWritable], reporter: Reporter) {
    val quad = parser.parseLine(quadString.toString)
    if(quad!=null && quad.predicate==Consts.SAMEAS_URI)
      URITranslatorHelperMethods.extractAndOutputSameAsPairs(quad.subject.value, quad.value.value, output)
  }
}
