package test

import de.fuberlin.wiwiss.ldif.mapreduce.mappers._
import org.apache.hadoop.fs.Path
import org.apache.hadoop.mapred._
import org.apache.hadoop.util._
import org.apache.hadoop.conf._
import org.apache.commons.io.FileUtils
import org.apache.hadoop.io.{IntWritable, Text}
import de.fuberlin.wiwiss.ldif.mapreduce.types._
import java.math.BigInteger
import de.fuberlin.wiwiss.r2r._
import ldif.entity.EntityDescription
import de.fuberlin.wiwiss.ldif.mapreduce.{EntityDescriptionMetaDataExtractor, EntityDescriptionMetadata}
import de.fuberlin.wiwiss.ldif.mapreduce.utils.HadoopHelper
import scala.collection.JavaConversions._
import java.io.{ObjectOutputStream, File}
import de.fuberlin.wiwiss.ldif.mapreduce.io._

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 10/6/11
 * Time: 4:42 PM
 * To change this template use File | Settings | File Templates.
 */


class RunHadoop extends Configured with Tool {
  def run(args: Array[String]): Int = {
    val conf = getConf
    val job = new JobConf(conf, classOf[RunHadoop])

//    job.setJarByClass(classOf[RunHadoop])
    job.setNumReduceTasks(0)
    job.setMapperClass(classOf[ProcessQuadsMapper])
    job.setMapOutputKeyClass(classOf[IntWritable])
    job.setMapOutputValueClass(classOf[ValuePathWritable])
    job.setOutputKeyClass(classOf[IntWritable])
    job.setOutputValueClass(classOf[ValuePathWritable])
    job.setOutputFormat(classOf[ValuePathMultipleSequenceFileOutput])

    val in = new Path(args(0))
    val out = new Path(args(1))
    FileInputFormat.addInputPath(job, in)
    FileOutputFormat.setOutputPath(job, out)

    JobClient.runJob(job)

    return 0
  }
}

object RunHadoop {
  private def getEntityDescriptions: Seq[EntityDescription] = {
    val mappingSource = new FileOrURISource("mappings.ttl")
    val uriGenerator = new EnumeratingURIGenerator("http://www4.wiwiss.fu-berlin.de/ldif/imported", BigInteger.ONE);
    val importedMappingModel = Repository.importMappingDataFromSource(mappingSource, uriGenerator)
    val repository = new Repository(new JenaModelSource(importedMappingModel))
    (for(mapping <- repository.getMappings.values) yield LDIFMapping(mapping).entityDescription).toSeq
  }

  def main(args: Array[String]) {
    println("Starting...")
    val entityDescriptions = getEntityDescriptions
    val edmd = (new EntityDescriptionMetaDataExtractor).extract(entityDescriptions)

    FileUtils.deleteDirectory(new File(args(1)))
    val start = System.currentTimeMillis
    val conf = new Configuration
    HadoopHelper.distributeSerializableObject(edmd, conf, "edmd")
    val res = ToolRunner.run(conf, new RunHadoop(), args)
    println("That's it. Took " + (System.currentTimeMillis-start)/1000.0 + "s")
    sys.exit(res)
  }
}