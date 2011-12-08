package ldif.hadoop.reducers

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.mapred.lib.MultipleOutputs
import org.apache.hadoop.mapred._
import collection.mutable.HashSet
import ldif.hadoop.utils.{HadoopHelper, URITranslatorHelperMethods}
import java.util.Iterator
import ldif.entity.entityComparator.entityComparator
import org.apache.hadoop.io.{Text, WritableUtils, NullWritable}
import ldif.hadoop.types.{QuadWritable, SameAsPairWritable}
import ldif.entity.{Node, NodeWritable}
import ldif.util.{Consts, UriMintHelper}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 12/1/11
 * Time: 11:47 AM
 * To change this template use File | Settings | File Templates.
 */

class UriMintValuePickerReducer extends MapReduceBase with Reducer[NodeWritable, Text, NullWritable, QuadWritable] {
  private var config: Configuration = null
  private var mos: MultipleOutputs = null
  private var mintNamespace: String = null
  private val sameAsQuad = new QuadWritable(null, new Text(Consts.SAMEAS_URI), null, new Text("Minting"))

  override def configure(conf: JobConf) {
    config = conf
    mos = new MultipleOutputs(conf)
    mintNamespace = HadoopHelper.getDistributedObject(conf, "mintNamespace").asInstanceOf[String]
  }

  override def reduce(entity: NodeWritable, values : Iterator[Text], output: OutputCollector[NullWritable, QuadWritable], reporter: Reporter) {
    var max = values.next().toString
    while(values.hasNext) {
      val value = values.next().toString
      if(entityComparator.lessThan(max, value))
        max = value
    }
    val mintedNode = new NodeWritable(mintNamespace+UriMintHelper.mintURI (mintNamespace, max), null, Node.UriNode, entity.graph)
    sameAsQuad.subject = entity
    sameAsQuad.obj = mintedNode
    output.collect(NullWritable.get(), sameAsQuad)
  }

  override def close() {
    mos.close()
  }
}