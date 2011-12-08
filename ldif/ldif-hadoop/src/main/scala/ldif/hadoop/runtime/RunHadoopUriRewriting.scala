package ldif.hadoop.runtime

import ldif.hadoop.utils.HadoopHelper
import org.apache.hadoop.conf.{Configuration, Configured}
import org.apache.hadoop.io.NullWritable
import org.apache.hadoop.mapred._
import org.slf4j.LoggerFactory
import org.apache.hadoop.util.{ToolRunner, Tool}
import org.apache.hadoop.fs.{FileSystem, Path}
import ldif.hadoop.types.QuadWritable
import ldif.hadoop.io._
import ldif.hadoop.mappers.UriRewritingMapper
import ldif.hadoop.reducers.UriRewritingReducer
import ldif.entity.NodeWritable
import ldif.util.Consts

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 12/1/11
 * Time: 5:00 PM
 * To change this template use File | Settings | File Templates.
 */

class RunHadoopUriRewriting extends Configured with Tool {
  def run(args: Array[String]): Int = {
    val conf = getConf
    val job = new JobConf(conf, classOf[RunHadoopUriRewriting])
    job.setJobName("UriRewriting")
    job.setMapperClass(classOf[UriRewritingMapper])
    job.setReducerClass(classOf[UriRewritingReducer])
    job.setMapOutputKeyClass(classOf[NodeWritable])
    job.setMapOutputValueClass(classOf[QuadWritable])
    job.setOutputKeyClass(classOf[NullWritable])
    job.setOutputValueClass(classOf[QuadWritable])

    job.setInputFormat(classOf[QuadSequenceFileInput])
    job.setOutputFormat(classOf[QuadSequenceFileOutput])

//    MultipleOutputs.addNamedOutput(job, "debug", classOf[QuadTextFileOutput], classOf[NullWritable], classOf[QuadWritable])

    var in = new Path(args(0))
    FileInputFormat.addInputPath(job, in)

    in = new Path(args(1))
    FileInputFormat.addInputPath(job, in)

    val out = new Path(args(2))
    FileOutputFormat.setOutputPath(job, out)

    JobClient.runJob(job)

    return 0
  }
}

object RunHadoopUriRewriting {
  private val log = LoggerFactory.getLogger(getClass.getName)

  def execute(datasetInputPath: String, sameAsLinksInputPath: String, outputPath: String): Int = {
    log.info("Starting Uri Rewriting...")
    val hadoopTmpDir = "hadoop_tmp"+Consts.fileSeparator+"urirewriting"
    val start = System.currentTimeMillis
    execute(datasetInputPath, sameAsLinksInputPath, hadoopTmpDir+"/rewrittenSubjects", false)
    val res = execute(hadoopTmpDir+"/rewrittenSubjects", sameAsLinksInputPath, outputPath, true)
    log.info("That's it. Uri Rewriting took " + (System.currentTimeMillis-start)/1000.0 + "s")
    res
  }

  def execute(datasetInputPath: String, sameAsLinksInputPath: String, outputPath: String, rewriteObjectUris: Boolean): Int = {
    val conf = new Configuration

    // remove existing output
    val hdfs = FileSystem.get(conf)
    val hdPath = new Path(outputPath)
    if (hdfs.exists(hdPath))
      hdfs.delete(hdPath, true)

    HadoopHelper.distributeSerializableObject(RewriteObjectUris(rewriteObjectUris), conf, "rewriteObjectUris")
    val res = ToolRunner.run(conf, new RunHadoopUriRewriting(), Array[String](datasetInputPath, sameAsLinksInputPath, outputPath))
    res
  }
}

case class RewriteObjectUris(value: Boolean)