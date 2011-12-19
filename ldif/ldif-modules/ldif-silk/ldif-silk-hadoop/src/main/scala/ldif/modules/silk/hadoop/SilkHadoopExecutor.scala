package ldif.modules.silk.hadoop

import ldif.module.Executor
import ldif.hadoop.runtime.{QuadFormat, StaticEntityFormat}
import ldif.modules.silk.{CreateEntityDescriptions, SilkTask}
import ldif.entity.EntityWritable
import org.apache.hadoop.mapreduce.lib.output.{SequenceFileOutputFormat, FileOutputFormat}
import org.apache.hadoop.mapreduce.Job
import org.apache.hadoop.mapreduce.lib.input.{FileInputFormat, SequenceFileInputFormat}
import org.apache.hadoop.fs.{FileSystem, Path}
import de.fuberlin.wiwiss.silk.util.DPair
import de.fuberlin.wiwiss.silk.hadoop.impl.{SilkOutputFormat, EntityConfidence}
import org.apache.hadoop.io.{BooleanWritable, IntWritable, Text}
import java.util.UUID

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
    val indexPath = new Path("silk_index/")
    //TODO find better way to create a unique path
    val sourceIndexPath = new Path(indexPath, task.name + "_source" + UUID.randomUUID.toString)
    val targetIndexPath = new Path(indexPath, task.name + "_target" + UUID.randomUUID.toString)

    runIndexingJob(task, reader(0), sourceIndexPath)
    runIndexingJob(task, reader(1), targetIndexPath)

    runLinkGenerationJob(task, DPair(sourceIndexPath, targetIndexPath), writer)
  }

  private def runIndexingJob(task: SilkTask, inputPath: Path, outputPath: Path) {
    val job = new Job()
    job.setJobName("Silk Indexing")

    // Distribute Configuration
    Config.write(job.getConfiguration, task.silkConfig.silkConfig, task.linkSpec)

    //Set Input
    FileInputFormat.setInputPaths(job, inputPath)
    job.setInputFormatClass(classOf[SequenceFileInputFormat[IntWritable, EntityWritable]])

    //Set Mapper
    job.setMapperClass(classOf[IndexMap])

    //Set Output
    val hdfs = FileSystem.get(job.getConfiguration)
    if (hdfs.exists(outputPath))
      hdfs.delete(outputPath, true)
    FileOutputFormat.setOutputPath(job, outputPath)

    job.setOutputFormatClass(classOf[SequenceFileOutputFormat[IndexWritable, EntityWritable]])
    job.setOutputKeyClass(classOf[IndexWritable])
    job.setOutputValueClass(classOf[EntityWritable])

    //Run job
    job.waitForCompletion(true)
  }

  private def runLinkGenerationJob(task: SilkTask, inputPaths: DPair[Path], outputPath: Path) {
    val job = new Job()
    job.setJobName("Silk Link Generation")

    // Distribute Configuration
    Config.write(job.getConfiguration, task.silkConfig.silkConfig, task.linkSpec)

    //Set Input
    job.getConfiguration.set("sourcePath", inputPaths.source.toString)
    job.getConfiguration.set("targetPath", inputPaths.target.toString)
    job.setInputFormatClass(classOf[EntityPairInputFormat])

    //Set Mapper and Reducer
    job.setMapperClass(classOf[ConfidenceMap])
    job.setReducerClass(classOf[FilterReduce])

    //Set Output
    val hdfs = FileSystem.get(job.getConfiguration)
    if (hdfs.exists(outputPath))
      hdfs.delete(outputPath, true)
    FileOutputFormat.setOutputPath(job, outputPath)

    job.setOutputFormatClass(classOf[SameAsOutputFormat])
    job.setOutputKeyClass(classOf[Text])
    job.setOutputValueClass(classOf[EntityConfidence])

    //Run job
    job.waitForCompletion(true)
  }
}