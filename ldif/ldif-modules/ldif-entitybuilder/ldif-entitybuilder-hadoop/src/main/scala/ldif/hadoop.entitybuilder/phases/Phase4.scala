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

package ldif.hadoop.entitybuilder.phases

import ldif.hadoop.entitybuilder.mappers._
import ldif.hadoop.entitybuilder.reducers._
import org.apache.hadoop.mapred._
import lib.MultipleOutputs
import org.apache.hadoop.util._
import org.apache.hadoop.conf._
import org.apache.commons.io.FileUtils
import org.apache.hadoop.io.IntWritable
import ldif.hadoop.types._
import java.io.File
import ldif.hadoop.entitybuilder.io._
import ldif.hadoop.utils.HadoopHelper
import org.apache.hadoop.fs.{FileSystem, Path}
import ldif.util.Consts
import ldif.entity.{EntityDescriptionMetadata, EntityDescription, EntityDescriptionMetaDataExtractor}
import java.util.logging.Logger
import ldif.hadoop.io.EntityMultipleTextFileOutput

/**
 *  Hadoop EntityBuilder - Phase 4
 *  Assembling entities
 **/

class Phase4 extends Configured with Tool {
  def run(args: Array[String]): Int = {
    val conf = getConf
    val fileSystem = FileSystem.get(conf)
    val job = new JobConf(conf, classOf[Phase4])
    val maxPhase = args(0).toInt

    job.setMapperClass(classOf[EntityConstructionMapper])
    job.setReducerClass(classOf[EntityConstructionReducer])
    job.setNumReduceTasks(1)

    job.setMapOutputKeyClass(classOf[EntityDescriptionNodeWritable])
    job.setMapOutputValueClass(classOf[ValuePathWritable])

    job.setOutputKeyClass(classOf[IntWritable])
    job.setOutputValueClass(classOf[ValuePathWritable])

    job.setInputFormat(classOf[ValuePathSequenceFileInput])
    job.setOutputFormat(classOf[EntityMultipleTextFileOutput])
    //Debugging
    MultipleOutputs.addNamedOutput(job, "debug", classOf[TextOutputFormat[IntWritable, ValuePathWritable]], classOf[IntWritable], classOf[ValuePathWritable])

    for(i <- 0 to math.max(0, maxPhase-1)) {
      var in = new Path(args(1) + Consts.fileSeparator + i + Consts.fileSeparator, ValuePathMultipleSequenceFileOutput.generateDirectoryNameForFinishedValuePaths(i))
      if(fileSystem.exists(in))
        FileInputFormat.addInputPath(job, in)
    }

    val out = new Path(args(2))
    FileOutputFormat.setOutputPath(job, out)

    JobClient.runJob(job)

    return 0
  }
}

object Phase4 {
  private val log = Logger.getLogger(getClass.getName)

  def runPhase(in : String, out : String, entityDescriptions : Seq[EntityDescription]) : Int = {
    val edmd = EntityDescriptionMetaDataExtractor.extract(entityDescriptions)
    runPhase(in,out,edmd)
  }

  def runPhase(in : String, out : String, edmd : EntityDescriptionMetadata) : Int = {
    log.info("Starting phase 4 of the EntityBuilder: assembling entities")

    FileUtils.deleteDirectory(new File(out))

    val start = System.currentTimeMillis
    val conf = new Configuration
    HadoopHelper.distributeSerializableObject(edmd, conf, "edmd")

    val maxPhase = edmd.maxPhase

    val res = ToolRunner.run(conf, new Phase4(), Array(maxPhase.toString, in, out))

    log.info("That's it. Took " + (System.currentTimeMillis-start)/1000.0 + "s")
    res
  }
}



