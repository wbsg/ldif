package ldif.modules.r2r.hadoop

import ldif.hadoop.types.QuadWritable
import org.apache.hadoop.io.{NullWritable, IntWritable}
import ldif.hadoop.utils.HadoopHelper
import org.apache.hadoop.mapred._
import java.io.{FileInputStream, ObjectInputStream}
import ldif.entity.{EntityDescriptionMetadata, EntityWritable}
import de.fuberlin.wiwiss.r2r.LDIFMapping

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 11/17/11
 * Time: 4:21 PM
 * To change this template use File | Settings | File Templates.
 */

class R2RMapper extends MapReduceBase with Mapper[IntWritable, EntityWritable, NullWritable, QuadWritable] {
  var mappings: IndexedSeq[LDIFMapping] = null

  override def configure(conf: JobConf) {
    mappings = getMappings(conf)
  }

  /**
   * @param key The entity description ID from which this entity was built
   * @param value An entity object
   */
  def map(key: IntWritable, value: EntityWritable, collector: OutputCollector[NullWritable, QuadWritable], reporter: Reporter) {
    val mapping = mappings(key.get())
    for(quad <- mapping.executeMapping(value))
      collector.collect(NullWritable.get(), new QuadWritable(quad))
  }

  private def getMappings(conf: JobConf): IndexedSeq[LDIFMapping] = {
    val file = HadoopHelper.getDistributedFilePathForID(conf, "mappings")
    return (new ObjectInputStream(new FileInputStream(file))).readObject().asInstanceOf[IndexedSeq[LDIFMapping]]
  }
}