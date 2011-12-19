package ldif.modules.silk.hadoop

import org.apache.hadoop.mapreduce.Reducer
import ldif.entity.EntityWritable
import de.fuberlin.wiwiss.silk.config.LinkSpecification
import org.apache.hadoop.io.IntWritable
import ldif.modules.silk.LdifEntity
import de.fuberlin.wiwiss.silk.util.DPair
import de.fuberlin.wiwiss.silk.entity.{EntityDescription, Entity}
import de.fuberlin.wiwiss.silk.cache.{Partition, BitsetIndex}

class IndexReduce extends Reducer[IntWritable, IndexedEntityWritable, IntWritable, PartitionWritable] {

  val partitionSize = 10000

  private var linkSpec: LinkSpecification = null

  private var entityDescs: DPair[EntityDescription] = null

  protected override def setup(context: Reducer[IntWritable, IndexedEntityWritable, IntWritable, PartitionWritable]#Context) {
    linkSpec = Config.readLinkSpec(context.getConfiguration)
    entityDescs = linkSpec.entityDescriptions
  }

  protected override def reduce(index : IntWritable, entities : java.lang.Iterable[IndexedEntityWritable],
                                context : Reducer[IntWritable, IndexedEntityWritable, IntWritable, PartitionWritable]#Context) {
    val iterator = entities.iterator()
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
        context.write(index, new PartitionWritable(Partition(currentEntities, currentIndices)))
        currentEntities = new Array[Entity](partitionSize)
        currentIndices = new Array[BitsetIndex](partitionSize)
      }
    }
    
    if(count > 0) {
      context.write(index, new PartitionWritable(Partition(currentEntities, currentIndices, count)))
    }
  }
}