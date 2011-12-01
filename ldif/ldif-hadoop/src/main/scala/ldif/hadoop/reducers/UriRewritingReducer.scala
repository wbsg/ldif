package ldif.hadoop.reducers

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.mapred.lib.MultipleOutputs
import ldif.hadoop.utils.HadoopHelper
import org.apache.hadoop.mapred._
import ldif.entity.entityComparator.entityComparator
import ldif.hadoop.types.QuadWritable
import java.util.Iterator
import collection.mutable.HashSet
import ldif.util.Consts
import org.apache.hadoop.io.{WritableUtils, NullWritable, Text}
import ldif.entity.{Node, NodeWritable}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 12/1/11
 * Time: 1:20 PM
 * To change this template use File | Settings | File Templates.
 */

class UriRewritingReducer extends MapReduceBase with Reducer[NodeWritable, QuadWritable, NullWritable, QuadWritable] {
  private var mos: MultipleOutputs = null
  private var config: Configuration = null
  private var rewriteObjectNode = false

  override def configure(conf: JobConf) {
    mos = new MultipleOutputs(conf)
    config = conf
    rewriteObjectNode = HadoopHelper.getDistributedObject(conf,"rewriteObjectUris").asInstanceOf[Boolean]
  }

  /**
   * This reducer rewrites either the subject or object part of all quads. The rewrite node is picked
   * from sameAs links (interpreted as directed links from left to right). These sameAs links are not
   * rewritten and also not output. The reducer also removes quad duplicates.
   */
  def reduce(entity: NodeWritable, quads: Iterator[QuadWritable], output: OutputCollector[NullWritable, QuadWritable], reporter: Reporter) {
    val quadSet = new HashSet[QuadWritable]
    var reWriteNode: NodeWritable = null
    while(quads.hasNext) {
      val quad: QuadWritable = quads.next()
      // Find the "largest" rewrite node in all (should usually be at most one) sameAs links
      // blank nodes are always smaller than nodes of other types
      if(quad.property==Consts.SAMEAS_URI && (reWriteNode==null || reWriteNode.compare(quad.obj) < 0))
        reWriteNode = WritableUtils.clone[NodeWritable](quad.obj, config)
      else
        quadSet.add(WritableUtils.clone[QuadWritable](quad, config))
    }

    for(quad <- quadSet) {
      // Don't rewrite quad if rewrite node is a blank node from another graph than the quad itself
      if(reWriteNode!=null && (reWriteNode.nodeType!=Node.BlankNode || reWriteNode.graph==quad.graph)) {
        if(rewriteObjectNode)
          quad.obj = reWriteNode
        else
          quad.subject = reWriteNode
      }
      output.collect(NullWritable.get(), quad)
    }
  }

  override def close() {
    mos.close()
  }
}