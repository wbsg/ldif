/* 
 * LDIF
 *
 * Copyright 2011-2012 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ldif.local.runtime

import ldif.module.DataFormat
import ldif.entity.EntityDescription

trait LocalDataFormat extends DataFormat

/**
 * Used when no input or output is needed e.g. the input of a data source.
 */
case class NoDataFormat() extends LocalDataFormat
{
  type Reader = Null
  type Writer = Null
}

/**
 * The Graph Format uses raw RDF Graphs to represent the data.
 */
case class GraphFormat() extends LocalDataFormat
{
  type Reader = Seq[QuadReader]
  type Writer = QuadWriter
}

/**
 * The dynamic entity format structures the data in entities of resources, where the format of the resource is dynamic
 * i.e. the task can process/output any format requested by the runtime.
 */
case class DynamicEntityFormat() extends LocalDataFormat
{
  type Reader = Seq[EntityReader]
  type Writer = Seq[EntityWriter]
}

/**
 * The static entity format structures the data in entities of resources, where the format of the resources is defined statically by the concrete task.
 * The Runtime transforms the input data to the specified entity description prior to providing it to the task.
 */
case class StaticEntityFormat(entityDescriptions : Seq[EntityDescription]) extends LocalDataFormat
{
  type Reader = Seq[EntityReader]
  type Writer = Seq[EntityWriter]
}