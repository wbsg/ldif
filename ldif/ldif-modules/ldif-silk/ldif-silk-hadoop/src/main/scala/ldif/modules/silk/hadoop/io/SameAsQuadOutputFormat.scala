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

import de.fuberlin.wiwiss.silk.hadoop.impl.EntityConfidence
import ldif.util.Consts
import org.apache.hadoop.mapred._
import org.apache.hadoop.fs.FileSystem
import org.apache.hadoop.util.Progressable
import ldif.hadoop.types.QuadWritable
import org.apache.hadoop.io.{SequenceFile, NullWritable, Text}
import ldif.entity.NodeWritable

class SameAsQuadOutputFormat extends SequenceFileOutputFormat[Text, EntityConfidence] {

  override def getRecordWriter(fs: FileSystem, job: JobConf, name: String, progress: Progressable): RecordWriter[Text, EntityConfidence] = {
    val file = FileOutputFormat.getTaskOutputPath(job, name)
    val fs = file.getFileSystem(job)
    val writer = SequenceFile.createWriter(fs, job, file, classOf[NullWritable], classOf[QuadWritable])
    new LinkQuadWriter(writer)
  }

  private class LinkQuadWriter(writer : SequenceFile.Writer) extends RecordWriter[Text, EntityConfidence] {
    val sameAsPredicate = new Text(Consts.SAMEAS_URI)
    val graph = new Text(Consts.SILK_OUT_GRAPH)

    override def write(sourceUri : Text, entitySimilarity : EntityConfidence) {
      val quad =  new QuadWritable(new NodeWritable(sourceUri.toString), sameAsPredicate, new NodeWritable(entitySimilarity.targetUri), graph)
      writer.append(NullWritable.get, quad)
    }

    override def close(reporter: Reporter) {
      writer.close()
    }
  }

}