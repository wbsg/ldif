package ldif.modules.silk.hadoop

import org.apache.hadoop.mapreduce.Mapper
import de.fuberlin.wiwiss.silk.hadoop.impl.EntityConfidence
import de.fuberlin.wiwiss.silk.config.LinkSpecification
import de.fuberlin.wiwiss.silk.util.DPair
import de.fuberlin.wiwiss.silk.entity.EntityDescription
import org.apache.hadoop.io.{IntWritable, BooleanWritable, Text, NullWritable}
import ldif.entity.EntityWritable
import ldif.modules.silk.LdifEntity

class ConfidenceMap extends Mapper[BooleanWritable, EntityPairWritable, Text, EntityConfidence] {

  private var linkSpec: LinkSpecification = null

  private var entityDescs: DPair[EntityDescription] = null

  protected override def setup(context: Mapper[BooleanWritable, EntityPairWritable, Text, EntityConfidence]#Context) {
    linkSpec = Config.readLinkSpec(context.getConfiguration)

    entityDescs = linkSpec.entityDescriptions
  }

  protected override def map(key : BooleanWritable, entities : EntityPairWritable, context : Mapper[BooleanWritable, EntityPairWritable, Text, EntityConfidence]#Context) {
    if(key.get) {
      val confidence = linkSpec.rule(DPair(new LdifEntity(entities.source, entityDescs.source), new LdifEntity(entities.target, entityDescs.target)), 0.0)

      if(confidence >= 0.0) {
        context.write(new Text(entities.source.resource.value), new EntityConfidence(confidence, entities.target.resource.value))
      }
    }
  }
}