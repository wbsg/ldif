package ldif.hadoop.runtime

import org.apache.hadoop.conf.Configured._
import ldif.hadoop.types.QuadWritable
import org.apache.commons.io.FileUtils
import java.io.File
import org.apache.hadoop.conf.{Configuration, Configured}
import org.apache.hadoop.util.{ToolRunner, Tool}
import org.apache.hadoop.mapred._
import ldif.hadoop.io.{QuadSequenceFileInput, QuadSequenceFileOutput}
import lib.{NullOutputFormat, MultipleOutputs, IdentityMapper}
import org.apache.hadoop.io.{Text, NullWritable}
import org.apache.hadoop.fs.{FileSystem, Path}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 12/8/11
 * Time: 5:39 PM
 * To change this template use File | Settings | File Templates.
 */

class RunHadoopQuadToTextConverter extends Configured with Tool {
  def run(args: Array[String]): Int = {
    val conf = getConf
    val job = new JobConf(conf, classOf[RunHadoopUriRewriting])

    job.setMapperClass(classOf[IdentityMapper[NullWritable, QuadWritable]])
    job.setNumReduceTasks(0)

    job.setMapOutputKeyClass(classOf[NullWritable])
    job.setMapOutputValueClass(classOf[QuadWritable])
    job.setOutputKeyClass(classOf[NullWritable])
    job.setOutputValueClass(classOf[Text])

    job.setInputFormat(classOf[QuadSequenceFileInput])
    job.setOutputFormat(classOf[TextOutputFormat[NullWritable, QuadWritable]])

    var in = new Path(args(0))
    FileInputFormat.addInputPath(job, in)

    val out = new Path(args(1))
    FileOutputFormat.setOutputPath(job, out)

    JobClient.runJob(job)

    return 0
  }
}

object RunHadoopQuadToTextConverter {

  def execute(inputPath: String, outputPath: String): Int = {
    println("Starting quad to N-Quads conversion...")
    val start = System.currentTimeMillis
    val conf = new Configuration

    // remove existing output
    val hdfs = FileSystem.get(conf)
    val hdPath = new Path(outputPath)
    if (hdfs.exists(hdPath))
      hdfs.delete(hdPath, true)

    val res = ToolRunner.run(conf, new RunHadoopQuadToTextConverter, Array[String](inputPath, outputPath))
    println("That's it. quad conversion took " + (System.currentTimeMillis-start)/1000.0 + "s")
    res
  }

  def main(args: Array[String]) {
    execute("r2rOutput", "r2rTest")
  }
}