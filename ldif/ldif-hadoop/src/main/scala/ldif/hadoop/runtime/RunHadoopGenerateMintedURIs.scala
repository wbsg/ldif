package ldif.hadoop.runtime

import org.apache.hadoop.conf.Configured._
import ldif.entity.NodeWritable
import ldif.hadoop.types.QuadWritable
import ldif.hadoop.io.{QuadSequenceFileOutput, QuadSequenceFileInput}
import org.apache.hadoop.mapred.{JobClient, FileOutputFormat, FileInputFormat, JobConf}
import org.slf4j.LoggerFactory
import ldif.util.Consts
import org.apache.hadoop.conf.{Configuration, Configured}
import org.apache.hadoop.fs.{FileSystem, Path}
import ldif.hadoop.utils.HadoopHelper
import org.apache.hadoop.util.{ToolRunner, Tool}
import ldif.hadoop.mappers.{UriMintValuePickMapper, UriRewritingMapper}
import ldif.hadoop.reducers.{UriMintValuePickerReducer, UriRewritingReducer}
import org.apache.hadoop.io.{Text, NullWritable}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 12/8/11
 * Time: 2:48 PM
 * To change this template use File | Settings | File Templates.
 */

class RunHadoopGenerateMintedURIs extends Configured with Tool {
  def run(args: Array[String]): Int = {
    val conf = getConf
    val job = new JobConf(conf, classOf[RunHadoopUriRewriting])

    job.setMapperClass(classOf[UriMintValuePickMapper])
    job.setReducerClass(classOf[UriMintValuePickerReducer])
    job.setMapOutputKeyClass(classOf[NodeWritable])
    job.setMapOutputValueClass(classOf[Text])
    job.setOutputKeyClass(classOf[NullWritable])
    job.setOutputValueClass(classOf[QuadWritable])

    job.setInputFormat(classOf[QuadSequenceFileInput])
    job.setOutputFormat(classOf[QuadSequenceFileOutput])

    val in = new Path(args(0))
    FileInputFormat.addInputPath(job, in)

    val out = new Path(args(1))
    FileOutputFormat.setOutputPath(job, out)

    JobClient.runJob(job)

    return 0
  }
}

object RunHadoopGenerateMintedURIs {
  private val log = LoggerFactory.getLogger(getClass.getName)

  def execute(datasetInputPath: String, outputPath: String, mintNamespace: String, mintPropertySet: Set[String]): Int = {
    log.info("Starting To Generate Uri Minting Mappings...")
    val start = System.currentTimeMillis
    val conf = new Configuration

    // remove existing output
    val hdfs = FileSystem.get(conf)
    val hdPath = new Path(outputPath)
    if (hdfs.exists(hdPath))
      hdfs.delete(hdPath, true)

    HadoopHelper.distributeSerializableObject(mintPropertySet, conf, "mintPropertySet")
    HadoopHelper.distributeSerializableObject(mintNamespace, conf, "mintNamespace")
    val res = ToolRunner.run(conf, new RunHadoopGenerateMintedURIs(), Array[String](datasetInputPath, outputPath))

    log.info("That's it. Generation of Uri Minting Mappings took " + (System.currentTimeMillis-start)/1000.0 + "s")
    res
  }
}