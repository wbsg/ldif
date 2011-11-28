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

import ldif.hadoop.entitybuilder.mappers._
import ldif.hadoop.entitybuilder.reducers._
import org.apache.hadoop.mapred._
import lib.{MultipleOutputs, NullOutputFormat}
import org.apache.hadoop.util._
import org.apache.hadoop.conf._
import org.apache.commons.io.FileUtils
import org.apache.hadoop.io.{IntWritable, Text}
import ldif.hadoop.types._
import java.math.BigInteger
import de.fuberlin.wiwiss.r2r._
import scala.collection.JavaConversions._
import java.io.{ObjectOutputStream, File}
import ldif.hadoop.entitybuilder.io._
import ldif.entity.{EntityDescriptionMetaDataExtractor, EntityDescription}
import ldif.hadoop.utils.HadoopHelper
import org.apache.hadoop.fs.{FileSystem, Path}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 10/25/11
 * Time: 11:46 AM
 * To change this template use File | Settings | File Templates.
 */

class RunPhase3 extends Configured with Tool {
  def run(args: Array[String]): Int = {
    val conf = getConf
    val job = new JobConf(conf, classOf[RunPhase3])
    val fileSystem = FileSystem.get(conf)

    val maxPhase = args(0).toInt
    val phase = args(1).toInt

    job.setMapperClass(classOf[ValuePathJoinMapper])
    job.setReducerClass(classOf[ValuePathJoinReducer])
    job.setNumReduceTasks(1)

    job.setMapOutputKeyClass(classOf[PathJoinValueWritable])
    job.setMapOutputValueClass(classOf[ValuePathWritable])

    job.setOutputKeyClass(classOf[IntWritable])
    job.setOutputValueClass(classOf[ValuePathWritable])

    job.setInputFormat(classOf[ValuePathSequenceFileInput])
    job.setOutputFormat(classOf[NullOutputFormat[IntWritable, ValuePathWritable]])

    MultipleOutputs.addNamedOutput(job, "seq", classOf[ValuePathMultipleSequenceFileOutput], classOf[IntWritable], classOf[ValuePathWritable])
    // For debugging
    MultipleOutputs.addNamedOutput(job, "text", classOf[ValuePathMultipleTextFileOutput], classOf[IntWritable], classOf[ValuePathWritable])

    // Keep track if at least one input path was added
    var inputPathAdded = false

    /* Add the JoinPaths for this phase (which were put into phase: (phase+1))
     * Don't do this if there is no join phase (maxPhase==0)
      */
    if(maxPhase>0) {
      var in = new Path(args(2), JoinValuePathMultipleSequenceFileOutput.generateDirectoryName(phase+1))
      if(fileSystem.exists(in)) {
        FileInputFormat.addInputPath(job, in); inputPathAdded = true}
    }

    if(phase==0) {
      // Add the initial EntityPaths for first phase
      var in = new Path(args(2), JoinValuePathMultipleSequenceFileOutput.generateDirectoryName(phase))
      if(fileSystem.exists(in)) {
        FileInputFormat.addInputPath(job, in); inputPathAdded = true}
    } else {
      // Add the constructed EntityPaths from the previous phase
      var in = new Path(RunPhase3.generateOutputPath(args(3), phase-1), ValuePathMultipleSequenceFileOutput.generateDirectoryNameForValuePathsInConstruction(phase-1))
      if(fileSystem.exists(in)) {
        FileInputFormat.addInputPath(job, in); inputPathAdded = true}
    }


    val out = new Path(RunPhase3.generateOutputPath(args(3), phase))
    FileOutputFormat.setOutputPath(job, out)
    if(inputPathAdded)
      JobClient.runJob(job)

    return 0
  }
}

object RunPhase3 {
  private def getEntityDescriptions: Seq[EntityDescription] = {
//    val mappingSource = new FileOrURISource("mappings.ttl")
    val mappingSource = new FileOrURISource("test_10k/mappings/ALL-to-Wiki.r2r.ttl")
    val uriGenerator = new EnumeratingURIGenerator("http://www4.wiwiss.fu-berlin.de/ldif/imported", BigInteger.ONE);
    val importedMappingModel = Repository.importMappingDataFromSource(mappingSource, uriGenerator)
    val repository = new Repository(new JenaModelSource(importedMappingModel))
    (for(mapping <- repository.getMappings.values) yield LDIFMapping(mapping).entityDescription).toSeq
  }

  def main(args: Array[String]) {
    val res = runPhase(args)
    sys.exit(res)
  }

  def generateOutputPath(out: String,  phase: Int): String = {
    out + "/" + phase
  }

  def runPhase(args: Array[String]): Int = {
    println("Starting phase 3 of the EntityBuilder: Joining value paths")
    val entityDescriptions = getEntityDescriptions
    val edmd = EntityDescriptionMetaDataExtractor.extract(entityDescriptions)

    val start = System.currentTimeMillis
    val conf = new Configuration
    HadoopHelper.distributeSerializableObject(edmd, conf, "edmd")

    var res = 0
    FileUtils.deleteDirectory(new File(args(1)))
    // maxPhase - 1 because: nrOfJoins == maxPhase - 1
    for(i <- 0 to math.max(0, edmd.maxPhase-1)) {
      println("Running iteration: " + i)
      println(generateOutputPath("Output directory: " + args(1), i))
      res = ToolRunner.run(conf, new RunPhase3(), (edmd.maxPhase.toString :: i.toString :: args.toList).toArray)
    }
    println("That's it. Took " + (System.currentTimeMillis-start)/1000.0 + "s")
    res
  }
}