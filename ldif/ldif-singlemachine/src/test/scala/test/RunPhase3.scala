package test

import de.fuberlin.wiwiss.ldif.mapreduce.mappers._
import de.fuberlin.wiwiss.ldif.mapreduce.reducers._
import org.apache.hadoop.fs.Path
import org.apache.hadoop.mapred._
import lib.{MultipleOutputs, NullOutputFormat}
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
 * Date: 10/25/11
 * Time: 11:46 AM
 * To change this template use File | Settings | File Templates.
 */

class RunPhase3 extends Configured with Tool {
  def run(args: Array[String]): Int = {
    val conf = getConf
    val job = new JobConf(conf, classOf[RunPhase3])

    val phase = args(0).toInt

    job.setMapperClass(classOf[ValuePathJoinMapper])
    job.setReducerClass(classOf[ValuePathJoinReducer])
    job.setNumReduceTasks(1)

    job.setMapOutputKeyClass(classOf[PathJoinValueWritable])
    job.setMapOutputValueClass(classOf[ValuePathWritable])

    job.setOutputKeyClass(classOf[IntWritable])
    job.setOutputValueClass(classOf[ValuePathWritable])

    job.setInputFormat(classOf[ValuePathSequenceFileInput])
    job.setOutputFormat(classOf[NullOutputFormat[IntWritable, ValuePathWritable]])

    MultipleOutputs.addNamedOutput(job, "seq", classOf[ValuePathMultipleSequenceFileOutput], classOf[IntWritable], classOf[ValuePathWritable])
    // For debugging
    MultipleOutputs.addNamedOutput(job, "text", classOf[ValuePathMultipleTextFileOutput], classOf[IntWritable], classOf[ValuePathWritable])

    // Add the JoinPaths for this phase (which were put into phase: (phase+1))
    var in = new Path(args(1), JoinValuePathMultipleSequenceFileOutput.generateDirectoryName(phase+1))
    FileInputFormat.addInputPath(job, in)

    if(phase==0) {
      // Add the initial EntityPaths for first phase
      in = new Path(args(1), JoinValuePathMultipleSequenceFileOutput.generateDirectoryName(phase))
      FileInputFormat.addInputPath(job, in)
    } else {
      // Add the constructed EntityPaths from the previous phase
      in = new Path(RunPhase3.generateOutputPath(args(2), phase-1), ValuePathMultipleSequenceFileOutput.generateDirectoryNameForValuePathsInConstruction(phase-1))
      FileInputFormat.addInputPath(job, in)
    }


    val out = new Path(RunPhase3.generateOutputPath(args(2), phase))
    FileOutputFormat.setOutputPath(job, out)

    JobClient.runJob(job)

    return 0
  }
}

object RunPhase3 {
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

    val start = System.currentTimeMillis
    val conf = new Configuration
    HadoopHelper.distributeSerializableObject(edmd, conf, "edmd")

    var res = 0
    // maxPhase - 1 because: nrOfJoins == maxPhase - 1
    for(i <- 0 to edmd.maxPhase-1) {
      println("Running phase: " + i)
      FileUtils.deleteDirectory(new File(generateOutputPath(args(1), i)))
      println(generateOutputPath("Output directory: " + args(1), i))
      res = ToolRunner.run(conf, new RunPhase3(), (i.toString :: args.toList).toArray)
    }
    println("That's it. Took " + (System.currentTimeMillis-start)/1000.0 + "s")
    sys.exit(res)
  }

  def generateOutputPath(out: String,  phase: Int): String = {
    out + "/" + phase
  }
}