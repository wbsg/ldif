package test

import de.fuberlin.wiwiss.ldif.mapreduce.mappers._
import org.apache.hadoop.fs.Path
import org.apache.hadoop.mapred._
import lib.{NullOutputFormat, MultipleOutputs}
import org.apache.hadoop.util._
import org.apache.hadoop.conf._
import org.apache.commons.io.FileUtils
import org.apache.hadoop.io.{IntWritable, Text}
import ldif.hadoop.types._
import java.math.BigInteger
import de.fuberlin.wiwiss.r2r._
import scala.collection.JavaConversions._
import java.io.{ObjectOutputStream, File}
import de.fuberlin.wiwiss.ldif.mapreduce.io._
import ldif.entity.{EntityDescriptionMetaDataExtractor, EntityDescription}
import ldif.hadoop.utils.HadoopHelper

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 10/6/11
 * Time: 4:42 PM
 * To change this template use File | Settings | File Templates.
 */


class RunPhase2 extends Configured with Tool {
  def run(args: Array[String]): Int = {
    val conf = getConf
    val job = new JobConf(conf, classOf[RunPhase2])

//    job.setJarByClass(classOf[RunHadoop])
    job.setNumReduceTasks(0)
    job.setMapperClass(classOf[ProcessQuadsMapper])
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

    JobClient.runJob(job)

    return 0
  }
}

object RunPhase2 {
  private def getEntityDescriptions: Seq[EntityDescription] = {
    val mappingSource = new FileOrURISource("mappings.ttl")
    val uriGenerator = new EnumeratingURIGenerator("http://www4.wiwiss.fu-berlin.de/ldif/imported", BigInteger.ONE);
    val importedMappingModel = Repository.importMappingDataFromSource(mappingSource, uriGenerator)
    val repository = new Repository(new JenaModelSource(importedMappingModel))
    (for(mapping <- repository.getMappings.values) yield LDIFMapping(mapping).entityDescription).toSeq
  }

  def main(args: Array[String]) {
    val res = runPhase(args)
    sys.exit(res)
  }

  def runPhase(args: Array[String]): Int = {
    println("Starting phase 2 of the EntityBuilder: Filtering quads and creating initial value paths")
    val entityDescriptions = getEntityDescriptions
    val edmd = EntityDescriptionMetaDataExtractor.extract(entityDescriptions)

    FileUtils.deleteDirectory(new File(args(1)))
    val start = System.currentTimeMillis
    val conf = new Configuration
    HadoopHelper.distributeSerializableObject(edmd, conf, "edmd")
    val res = ToolRunner.run(conf, new RunPhase2(), args)
    println("That's it. Took " + (System.currentTimeMillis-start)/1000.0 + "s")
    res
  }
}