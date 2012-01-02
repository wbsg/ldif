package ldif.modules.silk.hadoop

import io.{PartitionWritable, IndexedEntityWritable}
import org.apache.hadoop.mapred.Reducer
import de.fuberlin.wiwiss.silk.config.LinkSpecification
import ldif.modules.silk.LdifEntity
import de.fuberlin.wiwiss.silk.util.DPair
import de.fuberlin.wiwiss.silk.entity.{EntityDescription, Entity}
import de.fuberlin.wiwiss.silk.cache.{Partition, BitsetIndex}
import org.apache.hadoop.io.{Text, IntWritable}
import org.apache.hadoop.mapred.{Reporter, OutputCollector, JobConf, MapReduceBase}

class IndexReduce extends MapReduceBase 
                  with Reducer[IntWritable, IndexedEntityWritable, IntWritable, PartitionWritable]
                  with Configured {

  val partitionSize = 10000

  protected override def reduce(index : IntWritable,
                                iterator : java.util.Iterator[IndexedEntityWritable],
                                collector: OutputCollector[IntWritable, PartitionWritable],
                                reporter: Reporter) {
    var currentEntities = new Array[Entity](partitionSize)
    var currentIndices = new Array[BitsetIndex](partitionSize)
    var count = 0

    while(iterator.hasNext) {
      val next = iterator.next()
      currentEntities(count) = new LdifEntity(next.entity, entityDescs.target)
      currentIndices(count) = next.index
      count += 1
      if(count == partitionSize) {
        count = 0
        collector.collect(index, new PartitionWritable(Partition(currentEntities, currentIndices)))
        currentEntities = new Array[Entity](partitionSize)
        currentIndices = new Array[BitsetIndex](partitionSize)
      }
    }
    
    if(count > 0) {
      collector.collect(index, new PartitionWritable(Partition(currentEntities, currentIndices, count)))
    }
  }
}