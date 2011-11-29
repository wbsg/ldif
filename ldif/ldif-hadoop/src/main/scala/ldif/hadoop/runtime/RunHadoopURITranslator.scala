package ldif.hadoop.runtime

import org.apache.hadoop.mapred.lib.{MultipleOutputs, NullOutputFormat}
import org.apache.hadoop.fs.Path
import org.slf4j.LoggerFactory
import org.apache.commons.io.FileUtils
import java.io.File
import org.apache.hadoop.conf.{Configuration, Configured}
import ldif.hadoop.utils.HadoopHelper
import org.apache.hadoop.util.{ToolRunner, Tool}
import ldif.hadoop.mappers.ExtractSameAsPairsMapper
import ldif.hadoop.types.{SameAsPairWritable, ValuePathWritable}
import ldif.hadoop.reducers.JoinSameAsPairsReducer
import org.apache.hadoop.io.{NullWritable, Text, IntWritable}
import ldif.hadoop.io.{EntityMultipleTextFileOutput, SameAsPairTextOutputFormat, SameAsPairSequenceFileOutputFormat}
import ldif.entity.{EntityWritable, EntityDescriptionMetadata, EntityDescriptionMetaDataExtractor, EntityDescription}
import org.apache.hadoop.mapred._

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 11/29/11
 * Time: 11:07 AM
 * To change this template use File | Settings | File Templates.
 */

class RunHadoopURITranslator extends Configured with Tool {
  def run(args: Array[String]): Int = {
    val conf = getConf

    var job = setInitialSameAsPairsExtractorJob(conf, args(0), args(1)+"/iteration1")
    JobClient.runJob(job)

    return 0
  }

  private def setInitialSameAsPairsExtractorJob(conf: Configuration,inputPath: String, outputPath: String): JobConf = {
    val job = new JobConf(conf, classOf[RunHadoopURITranslator])
    job.setMapperClass(classOf[ExtractSameAsPairsMapper])
    job.setMapOutputKeyClass(classOf[Text])
    job.setMapOutputValueClass(classOf[SameAsPairWritable])
    job.setReducerClass(classOf[JoinSameAsPairsReducer])
    job.setOutputKeyClass(classOf[NullWritable])
    job.setOutputValueClass(classOf[SameAsPairWritable])
    job.setOutputFormat(classOf[SameAsPairSequenceFileOutputFormat])
    val in = new Path(inputPath)
    val out = new Path(outputPath)
    FileInputFormat.addInputPath(job, in)
    FileOutputFormat.setOutputPath(job, out)
    MultipleOutputs.addNamedOutput(job, "debug", classOf[TextOutputFormat[Text, SameAsPairWritable]], classOf[Text], classOf[SameAsPairWritable])
    job
  }
}

object RunHadoopURITranslator {
  private val log = LoggerFactory.getLogger(getClass.getName)

  def runHadoopURITranslator(in : String, out : String) : Int = {
    log.info("Starting URI Translator")

    FileUtils.deleteDirectory(new File(out))

    val start = System.currentTimeMillis
    val conf = new Configuration

    val res = ToolRunner.run(conf, new RunHadoopURITranslator(), Array(in, out))

    log.info("That's it. Took " + (System.currentTimeMillis-start)/1000.0 + "s")
    res
  }

  def main(args: Array[String]) {
    if(args.length>=2)
      runHadoopURITranslator(args(0), args(1))
  }
}