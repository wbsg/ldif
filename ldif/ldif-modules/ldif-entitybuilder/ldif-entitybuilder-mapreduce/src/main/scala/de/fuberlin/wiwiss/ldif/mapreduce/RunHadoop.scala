package de.fuberlin.wiwiss.ldif.mapreduce

import mappers.ProcessQuadsMapper
import org.apache.hadoop.fs.Path
import org.apache.hadoop.mapreduce.Job
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat
import org.apache.hadoop.util._
import org.apache.hadoop.conf._
import org.apache.commons.io.FileUtils
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat
import java.io.File
import org.apache.hadoop.io.{IntWritable, Text}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 10/6/11
 * Time: 4:42 PM
 * To change this template use File | Settings | File Templates.
 */


class RunHadoop extends Configured with Tool {
  def run(args: Array[String]): Int = {
    val config = getConf
    val job = new Job(config, "Run Hadoop")

    job.setJarByClass(classOf[RunHadoop])

    job.setMapperClass(classOf[ProcessQuadsMapper])
    job.setMapOutputKeyClass(classOf[IntWritable])
    job.setMapOutputValueClass(classOf[ValuePathWritable])

    val in = new Path(args(0))
    val out = new Path(args(1))
    FileInputFormat.addInputPath(job, in)
    FileOutputFormat.setOutputPath(job, out)

    return if(job.waitForCompletion(true)) 0 else 1
  }
}

object RunHadoop {
  def main(args: Array[String]) {
    println("Starting...")
    FileUtils.deleteDirectory(new File(args(1)))
    val start = System.currentTimeMillis
    val conf = new Configuration
    val res = ToolRunner.run(conf, new RunHadoop(), args)
    println("That's it. Took " + (System.currentTimeMillis-start)/1000.0 + "s")
    sys.exit(res)
  }
}