package ldif.modules.r2r.hadoop

import ldif.entity.EntityWritable
import ldif.hadoop.types.QuadWritable
import org.apache.hadoop.io.{NullWritable, IntWritable}
import org.apache.hadoop.mapred.{Reporter, OutputCollector, Mapper, MapReduceBase}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 11/17/11
 * Time: 4:21 PM
 * To change this template use File | Settings | File Templates.
 */

class R2RMapper extends MapReduceBase with Mapper[IntWritable, EntityWritable, NullWritable, QuadWritable] {


  def map(key: IntWritable, value: EntityWritable, collector: OutputCollector[NullWritable, QuadWritable], reporter: Reporter) {

  }
}