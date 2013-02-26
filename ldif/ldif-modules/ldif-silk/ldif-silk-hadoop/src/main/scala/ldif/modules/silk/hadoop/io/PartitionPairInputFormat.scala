package ldif.modules.silk.hadoop.io

/*
 * LDIF
 *
 * Copyright 2011-2013 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
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

import org.apache.hadoop.mapred._
import java.io.{DataInput, DataOutput}
import org.apache.hadoop.io.{IntWritable, Writable, BooleanWritable}
import org.apache.hadoop.fs.Path

class PartitionPairInputFormat extends InputFormat[BooleanWritable, PartitionPairWritable] {

  type PartitionInputFormat = SequenceFileInputFormat[IntWritable, PartitionWritable]

  private val inputFormat = new PartitionInputFormat()

  private val blockCount = 1000

  override def getSplits(job: JobConf, numSplits: Int): Array[InputSplit] = {
    Array.tabulate(blockCount)(block => getSplits(job, numSplits, block)).flatten
  }

  private def getSplits(job: JobConf, numSplits: Int, block: Int): Array[InputSplit] = {
    val sourcePath = new Path(job.get("sourcePath") + "/" + block)
    val targetPath = new Path(job.get("targetPath") + "/" + block)
    val fs = sourcePath.getFileSystem(job)

    if(fs.exists(sourcePath) && fs.exists(targetPath)) {
      job.set("mapred.input.dir", sourcePath.toString)
      val sourceSplits = inputFormat.getSplits(job, numSplits)

      job.set("mapred.input.dir", targetPath.toString)
      val targetSplits = inputFormat.getSplits(job, numSplits)

      for(s <- sourceSplits; t <- targetSplits) yield new PartitionPairSplit(s, t)
    }
    else {
      Array.empty
    }
  }

  override def getRecordReader(split: InputSplit, job: JobConf, reporter: Reporter): RecordReader[BooleanWritable, PartitionPairWritable] = {
    new PartitionPairReader(split.asInstanceOf[PartitionPairSplit], job, reporter)
  }

  private class PartitionPairReader(split: PartitionPairSplit, job: JobConf, reporter: Reporter) extends RecordReader[BooleanWritable, PartitionPairWritable] {

    private var sourceReader = inputFormat.getRecordReader(split.sourceSplit, job, reporter)

    private var targetReader = inputFormat.getRecordReader(split.targetSplit, job, reporter)

    private val currentSourceIndex = new IntWritable()

    private val currentTargetIndex = new IntWritable()

    private val currentSourceValue = new PartitionWritable()

    private val currentTargetValue = new PartitionWritable()

    //Read first target partition and set a flag if there is none
    private val isEmpty = !targetReader.next(currentTargetIndex, currentTargetValue)

    override def next(key: BooleanWritable, value: PartitionPairWritable): Boolean = {
      if(isEmpty)
        false
      else if(sourceReader.next(currentSourceIndex, currentSourceValue)) {
        setValues(key, value)
        true
      }
      else if(targetReader.next(currentTargetIndex, currentTargetValue)) {
        sourceReader.close()
        sourceReader = inputFormat.getRecordReader(split.sourceSplit, job, reporter)
        sourceReader.next(currentSourceIndex, currentSourceValue)
        setValues(key, value)
        true
      }
      else {
        false
      }
    }

    private def setValues(key: BooleanWritable, value: PartitionPairWritable) {
      key.set(currentSourceIndex.get == currentTargetIndex.get)
      value.source = currentSourceValue
      value.target = currentTargetValue
    }

    override def createKey() = new BooleanWritable()

    override def createValue() = new PartitionPairWritable()

    override def getProgress = targetReader.getProgress

    override def getPos = targetReader.getPos

    override def close() {
      sourceReader.close()
      targetReader.close()
    }
  }
}

class PartitionPairSplit(var sourceSplit: InputSplit, var targetSplit: InputSplit) extends InputSplit with Writable {

  def this() = this(new FileSplit(null, 0, 0, null: JobConf), new FileSplit(null, 0, 0, null: JobConf))

  override def getLength: Long = sourceSplit.getLength + targetSplit.getLength

  override def getLocations: Array[String] = sourceSplit.getLocations ++ targetSplit.getLocations

  override def write(out : DataOutput) {
    sourceSplit.asInstanceOf[Writable].write(out)
    targetSplit.asInstanceOf[Writable].write(out)
  }

  override def readFields(in : DataInput) {
    sourceSplit.asInstanceOf[Writable].readFields(in)
    targetSplit.asInstanceOf[Writable].readFields(in)
  }
}