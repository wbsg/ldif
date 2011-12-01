package ldif.hadoop.mappers

import ldif.datasources.dump.QuadParser
import org.apache.hadoop.mapred.lib.MultipleOutputs
import org.apache.hadoop.mapred._
import ldif.runtime.Quad
import ldif.util.Consts
import ldif.hadoop.utils.URITranslatorHelperMethods
import ldif.hadoop.types.{QuadWritable, SameAsPairWritable}
import ldif.entity.{Node, NodeWritable}
import org.apache.hadoop.io.{Text, NullWritable, LongWritable}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 12/1/11
 * Time: 6:29 PM
 * To change this template use File | Settings | File Templates.
 */

class ConvertSameAsPairsToQuadsMapper extends MapReduceBase with Mapper[NullWritable, SameAsPairWritable, NullWritable, QuadWritable] {
  private var mos: MultipleOutputs = null
  private val subj = new NodeWritable(null, null, Node.UriNode, "")
  private val obj = new NodeWritable(null, null, Node.UriNode, "")
  private val quad = new QuadWritable(subj, new Text(Consts.SAMEAS_URI), obj, new Text(""))

  override def configure(conf: JobConf) {
    mos = new MultipleOutputs(conf)
  }

  override def close() {
    mos.close()
  }

  override def map(nothing: NullWritable, sameAsPair: SameAsPairWritable, output: OutputCollector[NullWritable, QuadWritable], reporter: Reporter) {
    subj.value = sameAsPair.from
    obj.value = sameAsPair.to
    output.collect(NullWritable.get(), quad)
  }
}
