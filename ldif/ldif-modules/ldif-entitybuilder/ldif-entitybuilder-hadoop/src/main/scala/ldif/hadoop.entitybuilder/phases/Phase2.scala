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
import org.apache.hadoop.mapred._
import lib.{NullOutputFormat, MultipleOutputs}
import org.apache.hadoop.util._
import org.apache.hadoop.conf._
import ldif.hadoop.types._
import ldif.hadoop.entitybuilder.io._
import ldif.hadoop.utils.HadoopHelper
import ldif.entity.{EntityDescription, EntityDescriptionMetadata, EntityDescriptionMetaDataExtractor}
import org.slf4j.LoggerFactory
import ldif.hadoop.io.{QuadSequenceFileOutput, QuadSequenceFileInput}
import org.apache.hadoop.io.{NullWritable, IntWritable}
import org.apache.hadoop.fs.{FileSystem, Path}
import ldif.util.Consts
import ldif.hadoop.runtime.ConfigParameters

/**
 *  Hadoop EntityBuilder - Phase 2
 *  Filtering quads and creating initial value paths
 **/

class Phase2 extends Configured with Tool {
  def run(args: Array[String]): Int = {
    val conf = getConf
    val job = new JobConf(conf, classOf[Phase2])
    val getsTextInput = args(4).toBoolean

    job.setJobName("HEB-Phase2")

    //    job.setJarByClass(classOf[RunHadoop])
    job.setNumReduceTasks(0)
    if(getsTextInput)
      job.setMapperClass(classOf[ExtractAndProcessQuadsMapper])
    else {
      job.setMapperClass(classOf[ProcessQuadsMapper])
      job.setInputFormat(classOf[QuadSequenceFileInput])
    }
    job.setMapOutputKeyClass(classOf[IntWritable])
    job.setMapOutputValueClass(classOf[ValuePathWritable])
    job.setOutputKeyClass(classOf[IntWritable])
    job.setOutputValueClass(classOf[ValuePathWritable])
    job.setOutputFormat(classOf[NullOutputFormat[IntWritable, ValuePathWritable]])

    MultipleOutputs.addNamedOutput(job, "seq", classOf[JoinValuePathMultipleSequenceFileOutput], classOf[IntWritable], classOf[ValuePathWritable])
    MultipleOutputs.addNamedOutput(job, "text", classOf[JoinValuePathMultipleTextFileOutput], classOf[IntWritable], classOf[ValuePathWritable])

    val in = new Path(args(0))
    val out = new Path(args(1))
    FileInputFormat.addInputPath(job, in)
    FileOutputFormat.setOutputPath(job, out)

    // Check if sameAs links should be collected and add output collector
    if (args(2).toBoolean) {
      MultipleOutputs.addNamedOutput(job, "sameas", classOf[QuadSequenceFileOutput], classOf[NullWritable], classOf[QuadWritable])
      job.setBoolean("sameas", true)
    }
    // Check if irrelevant quads should be collected
    if (args(3).toBoolean ) {
      job.setBoolean("allquads", true)
    }
    // Check if provencance quads should be ignored
    if (args(5).toBoolean) {
      job.setBoolean("ignoreProvenance", true)
      job.setStrings("provenanceGraph", args(6))
    }

   // Add output collector for allQuads
    if (args(3).toBoolean || (!args(5).toBoolean)) {
      MultipleOutputs.addNamedOutput(job, "allquads", classOf[QuadSequenceFileOutput], classOf[NullWritable], classOf[QuadWritable])
    }

    JobClient.runJob(job)

    return 0
  }
}

object Phase2 {
  private val log = LoggerFactory.getLogger(getClass.getName)

  def runPhase(in : String, out : String, entityDescriptions : Seq[EntityDescription],  config : ConfigParameters) : Int = {
    val edmd = EntityDescriptionMetaDataExtractor.extract(entityDescriptions)
    runPhase(in,out,edmd, config)
  }

  def runPhase(in : String, out : String, edmd : EntityDescriptionMetadata, config : ConfigParameters) : Int = {

    // Don't collect sameAs/all quads if the destination paths are not defined (eg. while building entities for Silk)
    val useExternalSameAsLinks = config.configProperties.getProperty("useExternalSameAsLinks", "true").toLowerCase=="true" &&  config.sameAsPath != null
    val outputAllQuads = config.configProperties.getProperty("output", "mapped-only").toLowerCase=="all" &&  config.allQuadsPath != null
    val ignoreProvenance = config.configProperties.getProperty("outputFormat", "nq").toLowerCase=="nt"
    val provenanceGraph = config.configProperties.getProperty("provenanceGraph", Consts.DEFAULT_PROVENANCE_GRAPH)

    log.info("Starting phase 2 of the EntityBuilder: Filtering quads and creating initial value paths")

    val start = System.currentTimeMillis
    val conf = new Configuration
    HadoopHelper.distributeSerializableObject(edmd, conf, "edmd")

    // remove existing output
    val hdfs = FileSystem.get(conf)
    val hdPath = new Path(out)
    if (hdfs.exists(hdPath))
      hdfs.delete(hdPath, true)

    log.info("Output directory: " + out)
    val res = ToolRunner.run(conf, new Phase2(), Array[String](in, out, useExternalSameAsLinks.toString, outputAllQuads.toString, config.getsTextInput.toString, ignoreProvenance.toString, provenanceGraph))

    log.info("That's it. Took " + (System.currentTimeMillis-start)/1000.0 + "s")

    // move sameAs links quads to an ad-hoc directory
    if(useExternalSameAsLinks) {
        val outputFiles = hdfs.listStatus(hdPath).filterNot(_.isDir)
        val sameAsPath = new Path(config.sameAsPath)
        if (outputFiles.length > 0 && !hdfs.exists(sameAsPath))
          hdfs.mkdirs(sameAsPath)
        for (status <- outputFiles) {
          if(status.getPath.getName.startsWith("sameas"))
            hdfs.rename(status.getPath, new Path(config.sameAsPath+Consts.fileSeparator+status.getPath.getName))
      }
    }
    // move irrelevant quads to an ad-hoc directory
    if(outputAllQuads) {
      val outputFiles = hdfs.listStatus(hdPath).filterNot(_.isDir)
      val allQuadsPath = new Path(config.allQuadsPath)
      if (outputFiles.length > 0 && !hdfs.exists(allQuadsPath))
        hdfs.mkdirs(allQuadsPath)
      for (status <- outputFiles) {
        if(status.getPath.getName.startsWith("allquads"))
          hdfs.rename(status.getPath, new Path(config.allQuadsPath+Consts.fileSeparator+status.getPath.getName))
      }
    }

    res
  }
}