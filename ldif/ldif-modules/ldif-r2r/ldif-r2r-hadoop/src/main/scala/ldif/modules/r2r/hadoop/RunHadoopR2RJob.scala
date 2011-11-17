package ldif.modules.r2r.hadoop

import org.apache.hadoop.mapred.lib.{MultipleOutputs, NullOutputFormat}
import org.apache.hadoop.mapred.{JobClient, FileOutputFormat, FileInputFormat, JobConf}
import java.math.BigInteger
import de.fuberlin.wiwiss.r2r.{JenaModelSource, Repository, EnumeratingURIGenerator, FileOrURISource}
import de.fuberlin.wiwiss.r2r.LDIFMapping
import ldif.entity.{EntityDescriptionMetaDataExtractor, EntityDescription}
import org.apache.commons.io.FileUtils
import java.io.File
import org.apache.hadoop.conf.{Configuration, Configured}
import ldif.hadoop.utils.HadoopHelper
import org.apache.hadoop.util.{ToolRunner, Tool}
import org.apache.hadoop.io.{NullWritable, IntWritable}
import ldif.hadoop.types.{QuadWritable, ValuePathWritable}
import org.apache.hadoop.fs.{FileSystem, Path}
import ldif.hadoop.io.{EntityMultipleSequenceFileOutput, EntitySequenceFileInput, QuadSequenceFileOutput}
import scala.collection.JavaConversions._

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 11/17/11
 * Time: 5:39 PM
 * To change this template use File | Settings | File Templates.
 */

class RunHadoopR2RJob extends Configured with Tool {//TODO finish
  def run(args: Array[String]): Int = {
    val conf = getConf
    val job = new JobConf(conf, classOf[RunHadoopR2RJob])
    val fileSystem = FileSystem.get(conf)
    val nrOfEntityDescriptions = args(2).toInt
    val fileSeparator = System.getProperty("file.separator")

//    job.setJarByClass(classOf[RunHadoop])
    job.setNumReduceTasks(0)
    job.setMapperClass(classOf[R2RMapper])
    job.setMapOutputKeyClass(classOf[NullWritable])
    job.setMapOutputValueClass(classOf[QuadWritable])
    job.setOutputKeyClass(classOf[NullWritable])
    job.setOutputValueClass(classOf[QuadWritable])
    job.setOutputFormat(classOf[QuadSequenceFileOutput])

//    MultipleOutputs.addNamedOutput(job, "debug", classOf[QuadTextFileOutput], classOf[NullWritable], classOf[QuadWritable])

    for(i <- 0 until  nrOfEntityDescriptions) {
      val in = new Path(args(0), EntityMultipleSequenceFileOutput.generateDirectoryName(i))
      if(fileSystem.exists(in))
        FileInputFormat.addInputPath(job, in)
    }
    val out = new Path(args(1))
    FileOutputFormat.setOutputPath(job, out)

    JobClient.runJob(job)

    return 0
  }
}

object RunHadoopR2RJob {
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
    println("Starting R2R Job")
    val entityDescriptions = getEntityDescriptions
    val edmd = EntityDescriptionMetaDataExtractor.extract(entityDescriptions)

    FileUtils.deleteDirectory(new File(args(1)))
    val start = System.currentTimeMillis
    val conf = new Configuration
    HadoopHelper.distributeSerializableObject(edmd, conf, "edmd")
    val res = ToolRunner.run(conf, new RunHadoopR2RJob(), args)
    println("That's it. Took " + (System.currentTimeMillis-start)/1000.0 + "s")
    res
  }
}