package ldif.modules.silk.hadoop.io

/*
 * LDIF
 *
 * Copyright 2011 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
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
import org.apache.hadoop.io.Text
import ldif.util.Consts
import org.apache.hadoop.mapred._
import org.apache.hadoop.fs.FileSystem
import org.apache.hadoop.util.Progressable
import java.io.DataOutputStream


class SameAsOutputFormat extends SequenceFileOutputFormat[Text, EntityConfidence] {

  override def getRecordWriter(fs: FileSystem, job: JobConf, name: String, progress: Progressable): RecordWriter[Text, EntityConfidence] = {
    val file = FileOutputFormat.getTaskOutputPath(job, name)
    val fs = file.getFileSystem(job)
    val fileOut = fs.create(file, progress)
    new LinkWriter(fileOut)
  }

  private class LinkWriter(out: DataOutputStream) extends RecordWriter[Text, EntityConfidence] {
    override def write(sourceUri : Text, entitySimilarity : EntityConfidence) {
      val line = "<" + sourceUri + "> <http://www.w3.org/2002/07/owl#sameAs> <" + entitySimilarity.targetUri + "> <"+Consts.SILK_OUT_GRAPH+"> .\n"
      out.write(line.getBytes)
    }

    override def close(reporter: Reporter) {
      out.close()
    }
  }
}