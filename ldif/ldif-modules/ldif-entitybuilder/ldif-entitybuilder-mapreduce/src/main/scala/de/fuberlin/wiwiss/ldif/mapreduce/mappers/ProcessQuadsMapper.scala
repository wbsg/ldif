package de.fuberlin.wiwiss.ldif.mapreduce.mappers

import de.fuberlin.wiwiss.ldif.mapreduce._
import org.apache.hadoop.mapreduce.Mapper
import ldif.datasources.dump.QuadParser
import ldif.entity.NodeWritable
import org.apache.hadoop.io._
import de.fuberlin.wiwiss.ldif.mapreduce.types._
import utils.HadoopHelper
import org.apache.hadoop.conf.Configuration
import java.io.{ObjectInputStream, FileInputStream}

class ProcessQuadsMapper extends Mapper[LongWritable, Text, IntWritable, ValuePathWritable] {
  val parser = new QuadParser
  val values = new NodeArrayWritable
  var edmd: EntityDescriptionMetadata = null

  override def map(key: LongWritable, value: Text, context: Mapper[LongWritable, Text, IntWritable, ValuePathWritable]#Context) {
    if(edmd==null)
      edmd = getEntityDescriptionMetaData(context.getConfiguration)
    val quad = parser.parseLine(value.toString)
    if(quad==null)
      return
    val property = quad.predicate
    val propertyInfosValue = edmd.propertyMap.get(property)
    propertyInfosValue match {
      case None =>
      case Some(propertyInfos) =>
        for(propertyInfo <- propertyInfos) {
          val pathType = if (propertyInfo.phase==0) EntityPathType else JoinPathType
          val subj = new NodeWritable(quad.subject)
          val obj = new NodeWritable((quad.value))
          if(propertyInfo.isForward)
            values.set(Array[Writable](subj, obj))
          else
            values.set(Array[Writable](obj, subj))
          context.write(new IntWritable(propertyInfo.phase),
            new ValuePathWritable(new IntWritable(propertyInfo.pathId), pathType, values))
        }
    }
       //TODO: Implement MultiOutput
  }

  private def getEntityDescriptionMetaData(conf: Configuration): EntityDescriptionMetadata = {
    try {
      val file = HadoopHelper.getDistributedFilePathForID(conf, "edmd")
      return (new ObjectInputStream(new FileInputStream(file))).readObject().asInstanceOf[EntityDescriptionMetadata]
    } catch {
      case e: RuntimeException => throw new RuntimeException("No Entity Description Meta Data found/distributed. Reason: " + e.getMessage)
    }
  }
}
