/* 
 * Copyright 2011-2012 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ldif.hadoop.runtime

import org.apache.hadoop.mapred.lib.MultipleOutputs
import org.slf4j.LoggerFactory
import org.apache.hadoop.conf.{Configuration, Configured}
import org.apache.hadoop.util.{ToolRunner, Tool}
import org.apache.hadoop.io.{NullWritable, Text}
import org.apache.hadoop.mapred._
import collection.mutable.{Map, HashMap}
import scala.collection.JavaConversions._
import ldif.hadoop.utils.HadoopHelper
import ldif.hadoop.reducers.{WriteRemainingSameAsPairsReducer, JoinSameAsPairsReducer}
import ldif.hadoop.mappers.{ConvertSameAsPairsToQuadsMapper, WriteRemainingSameAsPairsMapper, SameAsPairsMapper, ExtractSameAsPairsMapper}
import ldif.hadoop.io._
import ldif.hadoop.types.{QuadWritable, SameAsPairWritable}
import ldif.util.Consts
import org.apache.hadoop.fs.{FileSystem, PathFilter, Path}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 11/29/11
 * Time: 11:07 AM
 * To change this template use File | Settings | File Templates.
 */

class RunHadoopURIClustering extends Configured with Tool {
  def run(args: Array[String]): Int = {
    val hadoopTmpDir = "hadoop_tmp"+Consts.fileSeparator+"uriclustering"

    val conf = getConf

    // remove existing output
    val hdfs = FileSystem.get(conf)
    val hdPath = new Path(hadoopTmpDir)
    if (hdfs.exists(hdPath))
      hdfs.delete(hdPath, true)

    var iteration = 1
    HadoopHelper.distributeSerializableObject(UriClusteringIteration(iteration), conf, "iteration")
    HadoopHelper.distributeSerializableObject(UriClusteringIteration(iteration+1), conf, "iteration"+iteration)

    var job = setInitialSameAsPairsExtractorJob(conf, args(0), hadoopTmpDir+"/iteration1")
    JobClient.runJob(job)

    var loop = true
    var clusterCounter: Map[Int, Long] = new HashMap[Int, Long]
    while(loop) {
      iteration+=1
      HadoopHelper.distributeSerializableObject(UriClusteringIteration(iteration), conf, "iteration"+iteration)
      job = setFollowingSameAsPairsJob(conf, hadoopTmpDir+"/iteration"+(iteration-1), hadoopTmpDir+"/iteration"+iteration)
      val runningJob = JobClient.runJob(job)
      val counters = runningJob.getCounters
      val newClusterCounter = computeClusterNumberBySizeCountersMap(counters)
      if(compareClusterCounters(clusterCounter, newClusterCounter))
        loop = false
      else
        clusterCounter = newClusterCounter
    }

    job = setFinishingSameAsPairsJob(conf, hadoopTmpDir+"/iteration"+iteration, hadoopTmpDir+"/iteration"+(iteration+1))
    JobClient.runJob(job)

    job = setConversionsJob(conf, hadoopTmpDir+"/iteration", args(1), iteration+1)
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

  private def setInitialSameAsPairsExtractorJob(conf: Configuration, inputPath: String, outputPath: String): JobConf = {
    val job = new JobConf(conf, classOf[RunHadoopURIClustering])
    job.setJobName("UriClustering-InitSameAsPairExtractor")
    job.setMapperClass(classOf[ExtractSameAsPairsMapper])
    job.setReducerClass(classOf[JoinSameAsPairsReducer])
    job.setInputFormat(classOf[QuadSequenceFileInput])
    setSameAsPairsJob(job, inputPath, outputPath)
  }

  private def setFollowingSameAsPairsJob(conf: Configuration, inputPath: String, outputPath: String): JobConf = {
    val job = new JobConf(conf, classOf[RunHadoopURIClustering])
    job.setJobName("UriClustering-FollowingSameAsPairs")
    job.setMapperClass(classOf[SameAsPairsMapper])
    job.setReducerClass(classOf[JoinSameAsPairsReducer])
    job.setInputFormat(classOf[SameAsPairSequenceFileInputFormat])
    FileInputFormat.setInputPathFilter(job, classOf[DebugFileExcludeFilter])
    setSameAsPairsJob(job, inputPath, outputPath)
  }

  private def setFinishingSameAsPairsJob(conf: Configuration, inputPath: String, outputPath: String): JobConf = {
    val job = new JobConf(conf, classOf[RunHadoopURIClustering])
    job.setJobName("UriClustering-FinishSameAsPairs")
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
//    MultipleOutputs.addNamedOutput(job, "debug", classOf[TextOutputFormat[Text, SameAsPairWritable]], classOf[Text], classOf[SameAsPairWritable])
    MultipleOutputs.addNamedOutput(job, "finished", classOf[SameAsPairSequenceFileOutputFormat], classOf[NullWritable], classOf[SameAsPairWritable])
    job
  }

  private def setConversionsJob(conf: Configuration, inputPath: String, outputPath: String, nrOfIteration: Int): JobConf = {
    val job = new JobConf(conf, classOf[RunHadoopURIClustering])
    job.setJobName("UriClustering-Conversions")
    job.setMapperClass(classOf[ConvertSameAsPairsToQuadsMapper])
    //job.setNumReduceTasks(0)
    job.setInputFormat(classOf[SameAsPairSequenceFileInputFormat])
    FileInputFormat.setInputPathFilter(job, classOf[FinishedClustersIncludeFilter])

    job.setMapOutputKeyClass(classOf[NullWritable])
    job.setMapOutputValueClass(classOf[QuadWritable])

    job.setOutputKeyClass(classOf[NullWritable])
    job.setOutputValueClass(classOf[QuadWritable])

    job.setOutputFormat(classOf[QuadSequenceFileOutput])

    for(i <- 1 to  nrOfIteration) {
      val in = new Path(inputPath+i)
      FileInputFormat.addInputPath(job, in)
    }

    val out = new Path(outputPath)
    FileOutputFormat.setOutputPath(job, out)

    job
  }
}

object RunHadoopURIClustering {
  private val log = LoggerFactory.getLogger(getClass.getName)

  def runHadoopURIClustering(in : String, out : String) : Int = {
    log.info("Starting URI Clustering...")

    val start = System.currentTimeMillis
    val conf = new Configuration

    // remove existing output
    val hdfs = FileSystem.get(conf)
    val hdPath = new Path(out)
    if (hdfs.exists(hdPath))
      hdfs.delete(hdPath, true)

    val res = ToolRunner.run(conf, new RunHadoopURIClustering(), Array(in, out))

    log.info("That's it. URI Clustering took " + (System.currentTimeMillis-start)/1000.0 + "s")
    res
  }

  def main(args: Array[String]) {
    if(args.length>=2)
      runHadoopURIClustering(args(0), args(1))
  }
}

class DebugFileExcludeFilter extends PathFilter {
  def accept(path: Path) = {
    path.getName.startsWith("part") || path.getName.startsWith("iteration")
  }
}

class FinishedClustersIncludeFilter extends PathFilter {
  def accept(path: Path) = {
    path.getName.startsWith("finish") || path.getName.startsWith("iteration")
  }
}