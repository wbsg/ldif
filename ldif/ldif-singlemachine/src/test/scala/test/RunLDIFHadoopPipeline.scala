/*
 * LDIF
 *
 * Copyright 2011 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
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

package test

import ldif.modules.r2r.hadoop.RunHadoopR2RJob
import ldif.entity.EntityDescription
import java.math.BigInteger
import de.fuberlin.wiwiss.r2r._
import scala.collection.JavaConversions._
import ldif.hadoop.entitybuilder.RunHadoopEntityBuilder

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 11/22/11
 * Time: 11:06 AM
 * To change this template use File | Settings | File Templates.
 */

object RunLDIFHadoopPipeline {
  def main(args: Array[String]) {
    val mappings = getMappings
    RunHadoopEntityBuilder.runHadoopEntityBuilder(args(0), args(1), getEntityDescriptions(mappings))
    RunHadoopR2RJob.runHadoopR2RJob(args(0), args(1), mappings)
  }

  def runHadoopLDIF(in: String, out: String, mappings: IndexedSeq[LDIFMapping]): Int = {
    val entityDescriptions = getEntityDescriptions(mappings)
    RunHadoopEntityBuilder.runHadoopEntityBuilder(in, out, entityDescriptions)
    RunHadoopR2RJob.runHadoopR2RJob(in, out, mappings)
  }


  private def getEntityDescriptions(mappings: IndexedSeq[LDIFMapping]): Seq[EntityDescription] = {
    for(mapping <- mappings) yield mapping.entityDescription
  }

    private def getMappings: IndexedSeq[LDIFMapping] = {
//    val mappingSource = new FileOrURISource("ldif-singlemachine/src/test/resources/mappings.ttl")
    val mappingSource = new FileOrURISource("test_10k/mappings/ALL-to-Wiki.r2r.ttl")

    val uriGenerator = new EnumeratingURIGenerator("http://www4.wiwiss.fu-berlin.de/ldif/imported", BigInteger.ONE);
    val importedMappingModel = Repository.importMappingDataFromSource(mappingSource, uriGenerator)
    val repository = new Repository(new JenaModelSource(importedMappingModel))
    (for(mapping <- repository.getMappings.values) yield LDIFMapping(mapping)).toIndexedSeq
  }
}