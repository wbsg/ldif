package ldif.modules.silk.hadoop

import org.apache.hadoop.mapreduce._
import ldif.entity.EntityWritable
import lib.input.{FileSplit, SequenceFileInputFormat}
import scala.collection.JavaConversions._
import collection.mutable.Buffer
import org.apache.hadoop.io.{Writable, BooleanWritable, IntWritable, NullWritable}
import java.io.{DataInput, DataOutput}

class EntityPairInputFormat extends InputFormat[BooleanWritable, EntityPairWritable] {
  
  type EntitySequenceFileInput = SequenceFileInputFormat[IndexWritable, EntityWritable]

  private val inputFormat = new EntitySequenceFileInput()
  
  override def getSplits(context : JobContext) : java.util.List[InputSplit] = {
    context.getConfiguration.set("mapred.input.dir", context.getConfiguration.get("sourcePath"))
    val sourceSplits = inputFormat.getSplits(context)

    context.getConfiguration.set("mapred.input.dir", context.getConfiguration.get("targetPath"))
    val targetSplits = inputFormat.getSplits(context)

    for(s <- sourceSplits; t <- targetSplits) yield new EntityPairSplit(s, t)
  }

  override def createRecordReader(inputSplit : InputSplit, context : TaskAttemptContext) : RecordReader[BooleanWritable, EntityPairWritable] = {
    new EntityPairReader(inputSplit.asInstanceOf[EntityPairSplit], context)
  }
  
  private class EntityPairReader(split: EntityPairSplit, taskContext: TaskAttemptContext) extends RecordReader[BooleanWritable, EntityPairWritable] {
    
    private var sourceReader: RecordReader[IndexWritable, EntityWritable] = null
    
    private var targetReader: RecordReader[IndexWritable, EntityWritable] = null
    
    override def getProgress = targetReader.getProgress

    override def initialize(inputSplit: InputSplit, context: TaskAttemptContext) {
      sourceReader = inputFormat.createRecordReader(split.sourceSplit, taskContext)
      targetReader = inputFormat.createRecordReader(split.targetSplit, taskContext)

      sourceReader.initialize(split.sourceSplit, taskContext)
      targetReader.initialize(split.targetSplit, taskContext)

      targetReader.nextKeyValue()

      //context.setStatus("Comparing partition " + ...)
    }

    override def close() {
      sourceReader = null
      targetReader = null
    }

    override def nextKeyValue: Boolean = {
      if(sourceReader.nextKeyValue()) {
        true
      }
      else if(targetReader.nextKeyValue()) {
        sourceReader.close()
        sourceReader = inputFormat.createRecordReader(split.sourceSplit, taskContext)
        sourceReader.initialize(split.sourceSplit, taskContext)
        sourceReader.nextKeyValue()
        true
      }
      else {
        false
      }
    }

    override def getCurrentKey = new BooleanWritable(!(sourceReader.getCurrentKey.indices intersect targetReader.getCurrentKey.indices).isEmpty)

    override def getCurrentValue = new EntityPairWritable(sourceReader.getCurrentValue, targetReader.getCurrentValue)
  }
}

class EntityPairSplit(var sourceSplit: InputSplit, var targetSplit: InputSplit) extends InputSplit with Writable {

  def this() = this(new FileSplit(null, 0, 0, null), new FileSplit(null, 0, 0, null))

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