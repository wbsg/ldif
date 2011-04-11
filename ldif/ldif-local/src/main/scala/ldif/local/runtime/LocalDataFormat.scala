package ldif.local.runtime

import ldif.module.DataFormat
import ldif.resource.ResourceFormat

trait LocalDataFormat extends DataFormat

/**
 * Used when no input or output is needed e.g. the input of a data source.
 */
case class NoDataFormat() extends LocalDataFormat
{
  type Reader = Nothing
  type Writer = Nothing
}

/**
 * The Graph Format uses raw RDF Graphs to represent the data.
 */
case class GraphFormat() extends LocalDataFormat
{
  type Reader = QuadReader
  type Writer = QuadWriter
}

/**
 * The dynamic resource format structures the data in buckets of resources, where the format of the resource is dynamic
 * i.e. the task can process/output any format requested by the runtime.
 */
case class DynamicInstanceFormat() extends LocalDataFormat
{
  type Reader = CacheReader
  type Writer = CacheWriter
}

/**
 * The static resource format structures the data in buckets of resources, where the format of the resources is defined statically by the concrete task.
 * The Runtime transforms the input data to the specified resource format prior to providing it to the task.
 */
case class StaticInstanceFormat(resourceFormats : Seq[ResourceFormat]) extends LocalDataFormat
{
  type Reader = Seq[CacheReader]
  type Writer = Seq[CacheWriter]
}








