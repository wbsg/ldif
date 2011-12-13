package ldif.modules.silk.hadoop

import org.apache.hadoop.mapreduce.Mapper
import de.fuberlin.wiwiss.silk.config.LinkSpecification
import de.fuberlin.wiwiss.silk.util.DPair
import ldif.modules.silk.LdifEntity
import ldif.entity.{EntityWritable}
import de.fuberlin.wiwiss.silk.entity.EntityDescription
import org.apache.hadoop.io.IntWritable

class IndexingPhase extends Mapper[IntWritable, EntityWritable, IndexWritable, EntityWritable] {

  private var linkSpec: LinkSpecification = null
  
  private var entityDescs: DPair[EntityDescription] = null
  
  protected override def setup(context: Mapper[IntWritable, EntityWritable, IndexWritable, EntityWritable]#Context) {
    linkSpec = Config.readLinkSpec(context.getConfiguration)
    
    entityDescs = linkSpec.entityDescriptions
  }
  
  protected override def map(key: IntWritable,
                             entity: EntityWritable,
                             context: Mapper[IntWritable, EntityWritable, IndexWritable, EntityWritable]#Context) {
    
    val index = linkSpec.rule.index(new LdifEntity(entity, entityDescs.select(key.get % 2 == 0)))
    context.write(new IndexWritable(index.flatten), entity)
  }
}