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

package ldif.hadoop.io

import org.apache.hadoop.io.IntWritable
import ldif.entity.EntityWritable
import org.apache.hadoop.mapred.lib.{MultipleSequenceFileOutputFormat, MultipleTextOutputFormat}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 11/17/11
 * Time: 6:08 PM
 * To change this template use File | Settings | File Templates.
 */

class EntityMultipleSequenceFileOutput extends MultipleSequenceFileOutputFormat[IntWritable, EntityWritable] {
  val fileSeparator = System.getProperty("file.separator")
  override def generateFileNameForKeyValue(key: IntWritable, value: EntityWritable, filename: String): String = {
    EntityMultipleSequenceFileOutput.generateDirectoryName(key.get) + fileSeparator + filename
  }
}

object EntityMultipleSequenceFileOutput {
  def generateDirectoryName(entityDescriptionID: Int) = "eb_entities_for_ed_" + entityDescriptionID
}