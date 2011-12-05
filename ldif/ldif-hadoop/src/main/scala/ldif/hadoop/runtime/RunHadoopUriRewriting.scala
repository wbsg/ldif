package ldif.hadoop.runtime

import org.apache.hadoop.conf.Configured._
import ldif.hadoop.utils.HadoopHelper
import collection.mutable.HashMap
import org.apache.hadoop.conf.{Configuration, Configured}
import org.apache.hadoop.io.{NullWritable, Text}
import org.apache.hadoop.mapred._
import lib.MultipleOutputs
import org.slf4j.LoggerFactory
import org.apache.commons.io.FileUtils
import java.io.File
import org.apache.hadoop.util.{ToolRunner, Tool}
import org.apache.hadoop.fs.{FileSystem, Path}
import ldif.hadoop.types.{QuadWritable, SameAsPairWritable}
import ldif.hadoop.io._
import ldif.hadoop.mappers.{UriRewritingMapper, WriteRemainingSameAsPairsMapper, SameAsPairsMapper, ExtractSameAsPairsMapper}
import ldif.hadoop.reducers.{UriRewritingReducer, WriteRemainingSameAsPairsReducer, JoinSameAsPairsReducer}
import ldif.entity.NodeWritable

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

  def execute(datasetInputPath: String, sameAsLinksInputPath: String, outputPath: String): Int = {//TODO: Add using temp directory for intermediary results (also for other jobs)
    println("Starting Uri Rewriting...")
    val start = System.currentTimeMillis
    execute(datasetInputPath, sameAsLinksInputPath, outputPath+"/rewrittenSubjects", false)
    val res = execute(outputPath+"/rewrittenSubjects", sameAsLinksInputPath, outputPath+"/output", true)
    println("That's it. Uri Rewriting took " + (System.currentTimeMillis-start)/1000.0 + "s")
    res
  }

  def execute(datasetInputPath: String, sameAsLinksInputPath: String, outputPath: String, rewriteObjectUris: Boolean): Int = {
    FileUtils.deleteDirectory(new File(outputPath))
    val conf = new Configuration
    HadoopHelper.distributeSerializableObject(RewriteObjectUris(rewriteObjectUris), conf, "rewriteObjectUris")
    val res = ToolRunner.run(conf, new RunHadoopUriRewriting(), Array[String](datasetInputPath, sameAsLinksInputPath, outputPath))
    res
  }
}

case class RewriteObjectUris(value: Boolean)