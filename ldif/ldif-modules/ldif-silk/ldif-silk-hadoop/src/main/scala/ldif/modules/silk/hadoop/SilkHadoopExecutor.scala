package ldif.modules.silk.hadoop

import ldif.module.Executor
import ldif.hadoop.runtime.{QuadFormat, StaticEntityFormat}
import ldif.modules.silk.{CreateEntityDescriptions, SilkTask}
import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.{IntWritable, Text}
import ldif.entity.EntityWritable
import org.apache.hadoop.mapreduce.lib.output.{SequenceFileOutputFormat, FileOutputFormat}
import org.apache.hadoop.mapreduce.Job
import org.apache.hadoop.mapreduce.lib.input.{FileInputFormat, SequenceFileInputFormat}

class SilkHadoopExecutor extends Executor {
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
    val job = new Job()
    setupJob(job)
    FileInputFormat.setInputPaths(job, reader.head)
    Config.writeLinkSpec(job.getConfiguration, task.linkSpec)
    job.waitForCompletion(true)
  }

  /**
   * Sets the Hadoop job up.
   */
  private def setupJob(job : Job) {
    //General settings
    job.setJobName("Silk Indexing")
    //job.setJarByClass(getClass)

    //Set Input
    job.setInputFormatClass(classOf[SequenceFileInputFormat[IntWritable, EntityWritable]])

    //Set Mapper
    job.setMapperClass(classOf[IndexingPhase])

    //Set Output
    FileOutputFormat.setOutputPath(job, new Path("silk_indexed"))

    job.setOutputFormatClass(classOf[SequenceFileOutputFormat[IntWritable, EntityWritable]])
    job.setOutputKeyClass(classOf[IntWritable])
    job.setOutputValueClass(classOf[EntityWritable])
  }
}