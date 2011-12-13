package ldif.modules.silk.hadoop

import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat
import org.apache.hadoop.io.Text
import de.fuberlin.wiwiss.silk.hadoop.impl.EntityConfidence
import org.apache.hadoop.mapreduce.{RecordWriter, TaskAttemptContext}
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat._
import de.fuberlin.wiwiss.silk.hadoop.SilkConfiguration
import java.io.DataOutputStream

class SameAsOutputFormat extends FileOutputFormat[Text, EntityConfidence] {

  override def getRecordWriter(job : TaskAttemptContext) : RecordWriter[Text, EntityConfidence] = {
    val config = job.getConfiguration
    val file = getDefaultWorkFile(job, ".nt")
    val fs = file.getFileSystem(config)
    val out = fs.create(file, false)

    new LinkWriter(out)
  }

  private class LinkWriter(out : DataOutputStream) extends RecordWriter[Text, EntityConfidence] {
    override def write(sourceUri : Text, entitySimilarity : EntityConfidence) {
      val line = "<" + sourceUri + "> <http://www.w3.org/2002/07/owl#sameAs> <" + entitySimilarity.targetUri + "> .\n"
      out.write(line.getBytes("UTF-8"))
    }

    override def close(context : TaskAttemptContext) {
      out.close()
    }
  }
}