package ldif.modules.silk.hadoop

import de.fuberlin.wiwiss.silk.hadoop.impl.EntityConfidence
import de.fuberlin.wiwiss.silk.config.LinkSpecification
import de.fuberlin.wiwiss.silk.util.DPair
import de.fuberlin.wiwiss.silk.entity.EntityDescription
import org.apache.hadoop.mapreduce.{Mapper, Reducer}
import org.apache.hadoop.io.{IntWritable, Text}
import ldif.entity.EntityWritable
import scala.collection.JavaConversions._

class FilterReduce extends Reducer[Text, EntityConfidence, Text, EntityConfidence] {

  private var linkSpec: LinkSpecification = null

  protected override def setup(context: Reducer[Text, EntityConfidence, Text, EntityConfidence]#Context) {
    linkSpec = Config.readLinkSpec(context.getConfiguration)
  }

  protected override def reduce(sourceUri : Text, entitiySimilarities : java.lang.Iterable[EntityConfidence],
                                context : Reducer[Text, EntityConfidence, Text, EntityConfidence]#Context) {

    linkSpec.filter.limit match {
      case Some(limit) => {
        for(entitySimilarity <- entitiySimilarities.take(limit)) {
          context.write(sourceUri, entitySimilarity)
        }
      }
      case None => {
        for(entitySimilarity <- entitiySimilarities) {
          context.write(sourceUri, entitySimilarity)
        }
      }
    }
  }
}