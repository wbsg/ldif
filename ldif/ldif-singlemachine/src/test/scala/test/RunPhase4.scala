package test

import de.fuberlin.wiwiss.ldif.mapreduce.mappers._
import de.fuberlin.wiwiss.ldif.mapreduce.reducers._
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
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.mapred._
import lib.MultipleOutputs

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
    val fileSystem = FileSystem.get(conf)
    val jobConf = new JobConf(conf, classOf[RunPhase4])
    val jobClient = new JobClient(jobConf)
    val maxPhase = args(0).toInt
    val fileSeparator = System.getProperty("file.separator")

    jobConf.setMapperClass(classOf[EntityConstructionMapper])
    jobConf.setReducerClass(classOf[EntityConstructionReducer])
    jobConf.setNumReduceTasks(1)

    jobConf.setMapOutputKeyClass(classOf[EntityDescriptionNodeWritable])
    jobConf.setMapOutputValueClass(classOf[ValuePathWritable])

    jobConf.setOutputKeyClass(classOf[IntWritable])
    jobConf.setOutputValueClass(classOf[ValuePathWritable])

    jobConf.setInputFormat(classOf[ValuePathSequenceFileInput])
    jobConf.setOutputFormat(classOf[EntityMultipleTextFileOutput])
    //Debugging
    MultipleOutputs.addNamedOutput(jobConf, "debug", classOf[TextOutputFormat[IntWritable, ValuePathWritable]], classOf[IntWritable], classOf[ValuePathWritable])

    for(i <- 0 to math.max(0, maxPhase-1)) {
      var in = new Path(args(1) + fileSeparator + i + fileSeparator, ValuePathMultipleSequenceFileOutput.generateDirectoryNameForFinishedValuePaths(i))
      if(fileSystem.exists(in))
        FileInputFormat.addInputPath(jobConf, in)
    }

    val out = new Path(args(2))
    FileOutputFormat.setOutputPath(jobConf, out)

    val runningJob = JobClient.runJob(jobConf)
//    val counters = runningJob.getCounters
//    val countValue=counters.getGroup("LDIF nr. of entities per ED").getCounter("ED ID 6")

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
    val edmd = EntityDescriptionMetaDataExtractor.extract(entityDescriptions)

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
