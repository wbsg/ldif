package ldif.hadoop.reducers

import ldif.hadoop.types.SameAsPairWritable
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.mapred.lib.MultipleOutputs
import org.apache.hadoop.mapred._
import collection.mutable.HashSet
import ldif.entity.NodeWritable
import ldif.hadoop.utils.{HadoopHelper, URITranslatorHelperMethods}
import java.util.Iterator
import ldif.entity.entityComparator.entityComparator
import org.apache.hadoop.io.{Text, WritableUtils, NullWritable}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 12/1/11
 * Time: 11:47 AM
 * To change this template use File | Settings | File Templates.
 */

class UriMintValuePickerReducer extends MapReduceBase with Reducer[NodeWritable, Text, NodeWritable, Text] {
  private var config: Configuration = null
  private var mos: MultipleOutputs = null
  private var mintNamespace: String = null

  override def configure(conf: JobConf) {
    config = conf
    mos = new MultipleOutputs(conf)
    mintNamespace = HadoopHelper.getDistributedObject(conf, "mintNamespace").asInstanceOf[String]
  }

  override def reduce(entity: NodeWritable, values : Iterator[Text], output: OutputCollector[NodeWritable, Text], reporter: Reporter) {
    var max = values.next().toString
    while(values.hasNext) {
      val value = values.next().toString
      if(entityComparator.lessThan(max, value))
        max = value
    }
    output.collect(entity, new Text(max))
  }

  override def close() {
    mos.close()
  }
}