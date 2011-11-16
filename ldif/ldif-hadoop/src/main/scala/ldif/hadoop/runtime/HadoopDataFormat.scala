package ldif.hadoop.runtime

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 11/16/11
 * Time: 2:43 PM
 * To change this template use File | Settings | File Templates.
 */

import ldif.module.DataFormat
import ldif.entity.EntityDescription
import org.apache.hadoop.fs.Path

class HadoopDataFormat extends DataFormat {
/**
 * Used when no input or output is needed e.g. the input of a data source.
 */
case class NoDataFormat() extends HadoopDataFormat
{
  type Reader = Null
  type Writer = Null
}

/**
 * The Quad Format uses RDF Graphs of quads to represent the data.
 */
case class GraphFormat() extends HadoopDataFormat
{
  type Reader = Seq[Path]
  type Writer = Path
}

/**
 * The entity format structures the data in entities of resources, where the format of the resources is defined statically by the concrete task.
 * The Runtime transforms the input data to the specified entity description prior to providing it to the task.
 */
case class EntityFormat(entityDescriptions : Seq[EntityDescription]) extends HadoopDataFormat
{
  type Reader = Seq[Path]
  type Writer = Seq[Path]
}
}