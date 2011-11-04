package test

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 10/27/11
 * Time: 12:12 PM
 * To change this template use File | Settings | File Templates.
 */
import de.fuberlin.wiwiss.ldif.mapreduce.mappers._
import de.fuberlin.wiwiss.ldif.mapreduce.reducers._
import org.apache.hadoop.fs.Path
import org.apache.hadoop.mapred._
import lib.MultipleOutputs
import org.apache.hadoop.util._
import org.apache.hadoop.conf._
import org.apache.commons.io.FileUtils
import org.apache.hadoop.io.{IntWritable, Text}
import ldif.mapreduce.types._
import java.math.BigInteger
import de.fuberlin.wiwiss.r2r._
import scala.collection.JavaConversions._
import java.io.{ObjectOutputStream, File}
import de.fuberlin.wiwiss.ldif.mapreduce.io._
import ldif.entity.{EntityDescriptionMetaDataExtractor, EntityDescription}
import ldif.mapreduce.utils.HadoopHelper

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 10/25/11
 * Time: 11:46 AM
 * To change this template use File | Settings | File Templates.
 */

class RunPhase4 extends Configured with Tool {
  def run(args: Array[String]): Int = {
    val conf = getConf
    val job = new JobConf(conf, classOf[RunPhase4])
    val maxPhase = args(0).toInt
    val fileSeparator = System.getProperty("file.separator")

    job.setMapperClass(classOf[EntityConstructionMapper])
    job.setReducerClass(classOf[EntityConstructionReducer])
    job.setNumReduceTasks(1)

    job.setMapOutputKeyClass(classOf[EntityDescriptionNodeWritable])
    job.setMapOutputValueClass(classOf[ValuePathWritable])

    job.setOutputKeyClass(classOf[IntWritable])
    job.setOutputValueClass(classOf[ValuePathWritable])

    job.setInputFormat(classOf[ValuePathSequenceFileInput])
    job.setOutputFormat(classOf[EntityMultipleTextFileOutput])
    //Debugging
    MultipleOutputs.addNamedOutput(job, "debug", classOf[TextOutputFormat[IntWritable, ValuePathWritable]], classOf[IntWritable], classOf[ValuePathWritable])

    for(i <- 0 to math.max(0, maxPhase-1)) {
      var in = new Path(args(1) + fileSeparator + i + fileSeparator, ValuePathMultipleSequenceFileOutput.generateDirectoryNameForFinishedValuePaths(i))
      FileInputFormat.addInputPath(job, in)
    }

    val out = new Path(args(2))
    FileOutputFormat.setOutputPath(job, out)

    JobClient.runJob(job)

    return 0
  }
}

object RunPhase4 {
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
    println("Starting phase 4 of the EntityBuilder: assembling entities")
    val entityDescriptions = getEntityDescriptions
    val edmd = (new EntityDescriptionMetaDataExtractor).extract(entityDescriptions)

    val start = System.currentTimeMillis
    val conf = new Configuration
    HadoopHelper.distributeSerializableObject(edmd, conf, "edmd")

    var res = 0
    val maxPhase = edmd.maxPhase
    FileUtils.deleteDirectory(new File(args(1)))
    res = ToolRunner.run(conf, new RunPhase4(), (maxPhase.toString :: args.toList).toArray)
    println("That's it. Took " + (System.currentTimeMillis-start)/1000.0 + "s")
    res
  }
}
