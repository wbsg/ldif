package ldif.modules.silk.hadoop

import org.apache.hadoop.mapreduce._
import ldif.entity.EntityWritable
import lib.input.SequenceFileInputFormat
import scala.collection.JavaConversions._
import collection.mutable.Buffer
import org.apache.hadoop.io.{BooleanWritable, IntWritable, NullWritable}

class EntityPairInputFormat extends InputFormat[BooleanWritable, EntityPairWritable] {
  
  type EntitySequenceFileInput = SequenceFileInputFormat[IntWritable, EntityWritable]

  private val inputFormat = new EntitySequenceFileInput()
  
  override def getSplits(context : JobContext) : java.util.List[InputSplit] = {
    context.getConfiguration.set("mapreduce.input.fileinputformat.inputdir", context.getConfiguration.get("sourcePath"))
    val sourceSplits = inputFormat.getSplits(context)

    context.getConfiguration.set("mapreduce.input.fileinputformat.inputdir", context.getConfiguration.get("targetPath"))
    val targetSplits = inputFormat.getSplits(context)

    for(s <- sourceSplits; t <- targetSplits) yield EntityPairSplit(s, t)
  }

  override def createRecordReader(inputSplit : InputSplit, context : TaskAttemptContext) : RecordReader[BooleanWritable, EntityPairWritable] = {
    new EntityPairReader(inputSplit.asInstanceOf[EntityPairSplit], context)
  }

  private case class EntityPairSplit(sourceSplit: InputSplit, targetSplit: InputSplit) extends InputSplit {
    
    override def getLength: Long = sourceSplit.getLength + targetSplit.getLength
    
    override def getLocations: Array[String] = sourceSplit.getLocations ++ targetSplit.getLocations
  }
  
  private class EntityPairReader(split: EntityPairSplit, taskContext: TaskAttemptContext) extends RecordReader[BooleanWritable, EntityPairWritable] {
    
    private var sourceReader: RecordReader[IntWritable, EntityWritable] = null
    
    private var targetReader: RecordReader[IntWritable, EntityWritable] = null
    
    override def getProgress = targetReader.getProgress

    override def initialize(inputSplit: InputSplit, context: TaskAttemptContext) {
      sourceReader = inputFormat.createRecordReader(split.sourceSplit, taskContext)
      targetReader = inputFormat.createRecordReader(split.targetSplit, taskContext)

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
        true
      }
      else {
        false
      }
    }

    override def getCurrentKey = new BooleanWritable(sourceReader.getCurrentKey.get ==targetReader.getCurrentKey.get)

    override def getCurrentValue = new EntityPairWritable(sourceReader.getCurrentValue, targetReader.getCurrentValue)
  }
}