package ldif.hadoop.runtime

import org.apache.hadoop.mapred.lib.{MultipleOutputs, NullOutputFormat}
import org.slf4j.LoggerFactory
import org.apache.commons.io.FileUtils
import java.io.File
import org.apache.hadoop.conf.{Configuration, Configured}
import org.apache.hadoop.util.{ToolRunner, Tool}
import ldif.hadoop.types.{SameAsPairWritable, ValuePathWritable}
import org.apache.hadoop.io.{NullWritable, Text, IntWritable}
import org.apache.hadoop.mapred._
import ldif.hadoop.io.{SameAsPairSequenceFileInputFormat, EntityMultipleTextFileOutput, SameAsPairTextOutputFormat, SameAsPairSequenceFileOutputFormat}
import org.apache.hadoop.fs.{PathFilter, Path}
import collection.mutable.{Map, HashMap}
import scala.collection.JavaConversions._
import ldif.hadoop.utils.HadoopHelper
import org.apache.hadoop.filecache.DistributedCache
import ldif.hadoop.mappers.{WriteRemainingSameAsPairsMapper, SameAsPairsMapper, ExtractSameAsPairsMapper}
import ldif.hadoop.reducers.{WriteRemainingSameAsPairsReducer, JoinSameAsPairsReducer}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 11/29/11
 * Time: 11:07 AM
 * To change this template use File | Settings | File Templates.
 */

class RunHadoopURIClustering extends Configured with Tool {
  def run(args: Array[String]): Int = {
    val conf = getConf
    var iteration = 1
    HadoopHelper.distributeSerializableObject(UriClusteringIteration(iteration), conf, "iteration")
        HadoopHelper.distributeSerializableObject(UriClusteringIteration(iteration+1), conf, "iteration"+iteration)


    var job = setInitialSameAsPairsExtractorJob(conf, args(0), args(1)+"/iteration1")
    JobClient.runJob(job)

    var loop = true
    var clusterCounter: Map[Int, Long] = new HashMap[Int, Long]
    while(loop) {
      iteration+=1
      HadoopHelper.distributeSerializableObject(UriClusteringIteration(iteration), conf, "iteration"+iteration)
      job = setFollowingSameAsPairsJob(conf, args(1)+"/iteration"+(iteration-1), args(1)+"/iteration"+iteration)
      val runningJob = JobClient.runJob(job)
      val counters = runningJob.getCounters
      val newClusterCounter = computeClusterNumberBySizeCountersMap(counters)
      if(compareClusterCounters(clusterCounter, newClusterCounter))
        loop = false
      else
        clusterCounter = newClusterCounter
    }

    job = setFinishingSameAsPairsJob(conf, args(1)+"/iteration"+iteration, args(1)+"/iteration"+(iteration+1))
    JobClient.runJob(job)

    return 0
  }

  private def compareClusterCounters(cCounters1: Map[Int, Long], cCounters2: Map[Int, Long]): Boolean = {
    if(!isSubSetOfClusterCounters(cCounters1, cCounters2))
      return false
    if(!isSubSetOfClusterCounters(cCounters2, cCounters1))
      return false
    return true
  }

  private def isSubSetOfClusterCounters(left: Map[Int, Long], right: Map[Int, Long]): Boolean = {
    for((counter, number) <- left) {
      if(!right.contains(counter))
        return false
      else if(right.get(counter).get!=number)
        return false
    }
    return true
  }

  private def computeClusterNumberBySizeCountersMap(counters: Counters): Map[Int, Long] = {
    var clusterNumberBySizeCounters = new HashMap[Int, Long]
    val clusterCounters = counters.getGroup("Cluster number by size")
    for(counter <- clusterCounters)
      clusterNumberBySizeCounters.put(counter.getName.trim().toInt, counter.getCounter)
    clusterNumberBySizeCounters
  }

  private def setInitialSameAsPairsExtractorJob(conf: Configuration,inputPath: String, outputPath: String): JobConf = {
    val job = new JobConf(conf, classOf[RunHadoopURIClustering])
    job.setMapperClass(classOf[ExtractSameAsPairsMapper])
    job.setReducerClass(classOf[JoinSameAsPairsReducer])
    setSameAsPairsJob(job, inputPath, outputPath)
  }

  private def setFollowingSameAsPairsJob(conf: Configuration,inputPath: String, outputPath: String): JobConf = {
    val job = new JobConf(conf, classOf[RunHadoopURIClustering])
    job.setMapperClass(classOf[SameAsPairsMapper])
    job.setReducerClass(classOf[JoinSameAsPairsReducer])
    job.setInputFormat(classOf[SameAsPairSequenceFileInputFormat])
    FileInputFormat.setInputPathFilter(job, classOf[DebugFileExcludeFilter])
    setSameAsPairsJob(job, inputPath, outputPath)
  }

  private def setFinishingSameAsPairsJob(conf: Configuration,inputPath: String, outputPath: String): JobConf = {
    val job = new JobConf(conf, classOf[RunHadoopURIClustering])
    job.setMapperClass(classOf[WriteRemainingSameAsPairsMapper])
    job.setReducerClass(classOf[WriteRemainingSameAsPairsReducer])
    job.setInputFormat(classOf[SameAsPairSequenceFileInputFormat])
    FileInputFormat.setInputPathFilter(job, classOf[DebugFileExcludeFilter])
    setSameAsPairsJob(job, inputPath, outputPath)
  }

  private def setSameAsPairsJob(job: JobConf, inputPath: String, outputPath: String): JobConf = {
    job.setMapOutputKeyClass(classOf[Text])
    job.setMapOutputValueClass(classOf[SameAsPairWritable])
    job.setOutputKeyClass(classOf[NullWritable])
    job.setOutputValueClass(classOf[SameAsPairWritable])
    job.setOutputFormat(classOf[SameAsPairSequenceFileOutputFormat])
    val in = new Path(inputPath)
    val out = new Path(outputPath)
    FileInputFormat.addInputPath(job, in)
    FileOutputFormat.setOutputPath(job, out)
    MultipleOutputs.addNamedOutput(job, "debug", classOf[TextOutputFormat[Text, SameAsPairWritable]], classOf[Text], classOf[SameAsPairWritable])
    MultipleOutputs.addNamedOutput(job, "finished", classOf[TextOutputFormat[NullWritable, SameAsPairWritable]], classOf[NullWritable], classOf[SameAsPairWritable])
    job
  }
}

object RunHadoopURIClustering {
  private val log = LoggerFactory.getLogger(getClass.getName)

  def runHadoopURITranslator(in : String, out : String) : Int = {
    log.info("Starting URI Translator")

    FileUtils.deleteDirectory(new File(out))

    val start = System.currentTimeMillis
    val conf = new Configuration

    val res = ToolRunner.run(conf, new RunHadoopURIClustering(), Array(in, out))

    log.info("That's it. Took " + (System.currentTimeMillis-start)/1000.0 + "s")
    res
  }

  def main(args: Array[String]) {
    if(args.length>=2)
      runHadoopURITranslator(args(0), args(1))
  }
}

class DebugFileExcludeFilter extends PathFilter {
  def accept(path: Path) = {
    path.getName.startsWith("part") || path.getName.startsWith("iteration")
  }
}