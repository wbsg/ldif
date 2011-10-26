package de.fuberlin.wiwiss.ldif.mapreduce.reducers

import de.fuberlin.wiwiss.ldif.mapreduce.EntityDescriptionMetadata
import org.apache.hadoop.mapred._
import de.fuberlin.wiwiss.ldif.mapreduce.utils.HadoopHelper
import lib.MultipleOutputs
import collection.mutable.ArrayBuffer
import de.fuberlin.wiwiss.ldif.mapreduce.types._
import org.apache.hadoop.io.{IntWritable, Writable}
import java.util.Iterator

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 10/25/11
 * Time: 12:35 PM
 * To change this template use File | Settings | File Templates.
 */

class ValuePathJoinReducer extends MapReduceBase with Reducer[PathJoinValueWritable, ValuePathWritable, IntWritable, ValuePathWritable] {
  var edmd: EntityDescriptionMetadata = null
  private var mos: MultipleOutputs = null
  private val pathIDWritable = new IntWritable
  private val nodeValues = new NodeArrayWritable
  private var collector: OutputCollector[IntWritable, ValuePathWritable] = null

  override def configure(conf: JobConf) {
    edmd = HadoopHelper.getEntityDescriptionMetaData(conf)
    mos = new MultipleOutputs(conf)
  }

  override def reduce(key: PathJoinValueWritable, values: Iterator[ValuePathWritable], output: OutputCollector[IntWritable, ValuePathWritable], reporter: Reporter) {
    val entityPaths = new ArrayBuffer[Array[Writable]]()
    val joinPaths = new ArrayBuffer[Writable]()
    val pathID = key.pathID.get
    pathIDWritable.set(pathID)
    val finalPathLength = edmd.pathLength(pathID)

    while(values.hasNext) {
      val value = values.next()
      value.pathType match {
        case EntityPathType => entityPaths.append(value.values.get)
        case JoinPathType => joinPaths.append(value.values.get()(1))
      }
    }

    for(entityPath <- entityPaths) {
      val nextPhase = entityPath.length - 2
      for(joinPath <- joinPaths) {
        nodeValues.set((entityPath ++ List(joinPath)).toArray)
        val pathType = {
          if(nodeValues.get.length-1 == finalPathLength)
            FinishedPathType
          else
            EntityPathType
        }
        val path = new ValuePathWritable(pathIDWritable, pathType, nodeValues)
        collector = mos.getCollector("seq", reporter).asInstanceOf[OutputCollector[IntWritable, ValuePathWritable]]
        collector.collect(new IntWritable(nextPhase), path)
        collector = mos.getCollector("text", reporter).asInstanceOf[OutputCollector[IntWritable, ValuePathWritable]]
        collector.collect(new IntWritable(nextPhase), path)
      }
    }
  }

  override def close() {
    mos.close()
  }
}