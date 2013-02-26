/*
 * LDIF
 *
 * Copyright 2011-2013 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
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

package ldif.modules.silk.hadoop

import io._
import ldif.module.Executor
import ldif.hadoop.runtime.{QuadFormat, StaticEntityFormat}
import ldif.modules.silk.{CreateEntityDescriptions, SilkTask}
import ldif.entity.EntityWritable
import org.apache.hadoop.fs.{FileSystem, Path}
import de.fuberlin.wiwiss.silk.util.DPair
import de.fuberlin.wiwiss.silk.hadoop.impl.EntityConfidence
import org.apache.hadoop.io.{IntWritable, Text}
import java.util.UUID
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.mapred._
import org.slf4j.LoggerFactory

class SilkHadoopExecutor extends Executor {
  private val log = LoggerFactory.getLogger(getClass.getName)
  type TaskType = SilkTask
  type InputFormat = StaticEntityFormat
  type OutputFormat = QuadFormat

  def input(task : SilkTask) = {
    implicit val prefixes = task.silkConfig.silkConfig.prefixes
    val entityDescriptions = CreateEntityDescriptions(task.linkSpec)

    new StaticEntityFormat(entityDescriptions)
  }

  def output(task: SilkTask) = new QuadFormat()


  override def execute(task: SilkTask, reader: Seq[Path], writer: Path) {
    val indexPath = new Path("silk_index/")
    val sourceIndexPath = new Path(indexPath, task.name + "_source_" + UUID.randomUUID.toString)
    val targetIndexPath = new Path(indexPath, task.name + "_target_" + UUID.randomUUID.toString)

    runIndexingJob(task, reader(0), sourceIndexPath, true)
    runIndexingJob(task, reader(1), targetIndexPath, false)

    runLinkGenerationJob(task, DPair(sourceIndexPath, targetIndexPath), writer)

    val hdfs = FileSystem.get(new Configuration)
    hdfs.delete(indexPath, true)

  }

  private def runIndexingJob(task: SilkTask, inputPath: Path, outputPath: Path, isSource: Boolean) {
    val job = new JobConf(new Configuration(), classOf[SilkHadoopExecutor])
    job.setJobName("Silk Indexing")
    val hdfs = FileSystem.get(job)

    // Distribute Configuration
    Configured.write(job, task.linkSpec, isSource)

    //Set Input
    if(hdfs.exists(inputPath))
      FileInputFormat.setInputPaths(job, inputPath)
    else {
      log.warn("Input path " + inputPath + " does not exist.")
      return
    }
    job.setInputFormat(classOf[SequenceFileInputFormat[IntWritable, EntityWritable]])

    //Set Mapper
    job.setMapperClass(classOf[IndexMap])
    job.setReducerClass(classOf[IndexReduce])

    job.setMapOutputKeyClass(classOf[IntWritable])
    job.setMapOutputValueClass(classOf[IndexedEntityWritable])

    //Set Output
    if (hdfs.exists(outputPath))
      hdfs.delete(outputPath, true)
    FileOutputFormat.setOutputPath(job, outputPath)

    job.setOutputFormat(classOf[PartitionPairOutputFormat])
    job.setOutputKeyClass(classOf[IntWritable])
    job.setOutputValueClass(classOf[PartitionWritable])

    //Run job
    JobClient.runJob(job)
  }

  private def runLinkGenerationJob(task: SilkTask, inputPaths: DPair[Path], outputPath: Path) {
    val job = new JobConf(new Configuration(), classOf[SilkHadoopExecutor])
    job.setJobName("Silk Link Generation")

    // Distribute Configuration
    Configured.write(job, task.linkSpec)

    //Set Input
    job.set("sourcePath", inputPaths.source.toString)
    job.set("targetPath", inputPaths.target.toString)
    job.setInputFormat(classOf[PartitionPairInputFormat])

    //Set Mapper and Reducer
    job.setMapperClass(classOf[MatchMap])
    job.setReducerClass(classOf[MatchReduce])

    //Set Output
    val hdfs = FileSystem.get(job)
    if (hdfs.exists(outputPath))
      hdfs.delete(outputPath, true)
    FileOutputFormat.setOutputPath(job, outputPath)

    job.setOutputFormat(classOf[SameAsQuadOutputFormat])
    job.setOutputKeyClass(classOf[Text])
    job.setOutputValueClass(classOf[EntityConfidence])

    //Run job
    JobClient.runJob(job)
  }
}