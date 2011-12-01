package ldif.hadoop.mappers

import ldif.hadoop.types.QuadWritable
import ldif.entity.NodeWritable
import org.apache.hadoop.io.{Text, NullWritable}
import org.apache.hadoop.mapred.lib.MultipleOutputs
import ldif.hadoop.utils.HadoopHelper
import org.apache.hadoop.mapred._
import ldif.util.Consts

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 12/1/11
 * Time: 12:54 PM
 * To change this template use File | Settings | File Templates.
 */

class UriRewritingMapper extends MapReduceBase with Mapper[NullWritable, QuadWritable, NodeWritable, QuadWritable] {
  private var mos: MultipleOutputs = null
  private var rewriteObjectNode = false

  override def configure(conf: JobConf) {
    mos = new MultipleOutputs(conf)
    rewriteObjectNode = HadoopHelper.getDistributedObject(conf,"rewriteObjectUris").asInstanceOf[Boolean]
  }

  /**
   * Collect all values of mint properties for each entity
   */
  override def map(key: NullWritable, quad: QuadWritable, output: OutputCollector[NodeWritable, QuadWritable], reporter: Reporter) {
    var nodeToRewrite: NodeWritable = null
    if(rewriteObjectNode && quad.property!=Consts.SAMEAS_URI)
      nodeToRewrite = quad.obj
    else
      nodeToRewrite = quad.subject
    output.collect(nodeToRewrite, quad)
  }

  override def close() {
    mos.close()
  }
}