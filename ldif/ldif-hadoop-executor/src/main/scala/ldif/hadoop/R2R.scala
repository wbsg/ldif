/*
 * LDIF
 *
 * Copyright 2011-2014 Universität Mannheim, MediaEvent Services GmbH & Co. KG
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

package ldif.hadoop

import org.apache.hadoop.fs.{Path, FileSystem}
import java.math.BigInteger
import java.io.File
import de.fuberlin.wiwiss.r2r._
import scala.collection.JavaConversions._
import ldif.modules.r2r.hadoop.RunHadoopR2RJob
import ldif.entity.EntityDescription
import org.apache.hadoop.conf.Configuration

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 2/7/12
 * Time: 6:37 PM
 * To change this template use File | Settings | File Templates.
 */

object R2R {
  def execute(args: Array[String]) {
    if(args.length < 3) {
      sys.error("Parameters: <mappings path> <input path> <output path>")
      sys.exit(1)
    }

    val mappingsPath = args(0)
    val input = args(1)
    val tempDir = "tmp_eb_output"+System.currentTimeMillis
    val output = args(2)

    // remove existing output
    clean(output)

    val mappings = getLdifMappings(mappingsPath)
    val entityDescriptions = getEntityDescriptions(mappings)

    EB.buildEntities(input, tempDir, entityDescriptions)

    RunHadoopR2RJob.execute(tempDir, output, mappings, standalone = true)
    clean(tempDir)
  }

  private def getLdifMappings(mappingsPath: String): IndexedSeq[LDIFMapping] = {
    val mappingSource = new FileOrURISource(new File(mappingsPath))
    val uriGenerator = new EnumeratingURIGenerator("http://www4.wiwiss.fu-berlin.de/ldif/imported", BigInteger.ONE);
    val importedMappingModel = Repository.importMappingDataFromSource(mappingSource, uriGenerator)
    val repository = new Repository(new JenaModelSource(importedMappingModel))
    return (for(mapping <- repository.getMappings.values()) yield LDIFMapping(mapping)).toIndexedSeq
  }

  private def getEntityDescriptions(mappings: IndexedSeq[LDIFMapping]): IndexedSeq[EntityDescription] = {
    (for(mapping <- mappings) yield mapping.entityDescription).toIndexedSeq
  }

  // Delete path/directory
  private def clean(hdPath: String) : Path =  {
    val path = new Path(hdPath)
    val hdfs = FileSystem.get(new Configuration())
    if (hdfs.exists(path))
      hdfs.delete(path, true)
    path
  }
}