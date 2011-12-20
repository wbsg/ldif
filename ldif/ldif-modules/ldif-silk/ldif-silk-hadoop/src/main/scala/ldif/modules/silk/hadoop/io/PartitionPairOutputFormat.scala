package ldif.modules.silk.hadoop.io

import org.apache.hadoop.mapred.lib.MultipleSequenceFileOutputFormat
import org.apache.hadoop.io.IntWritable

class PartitionPairOutputFormat extends MultipleSequenceFileOutputFormat[IntWritable, PartitionWritable] {

  override protected def generateFileNameForKeyValue(key: IntWritable, value: PartitionWritable, name: String): String = {
    key.get.toString + "/" + name
  }
}