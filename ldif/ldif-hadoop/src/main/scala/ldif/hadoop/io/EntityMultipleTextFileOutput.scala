/* 
 * LDIF
 *
 * Copyright 2011-2014 Universität Mannheim, MediaEvent Services GmbH & Co. KG
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

import org.apache.hadoop.mapred.lib.MultipleTextOutputFormat
import org.apache.hadoop.io.{NullWritable, IntWritable}
import ldif.entity.EntityWritable

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 10/27/11
 * Time: 12:32 PM
 * To change this template use File | Settings | File Templates.
 */

class EntityMultipleTextFileOutput extends MultipleTextOutputFormat[IntWritable, EntityWritable] {
  val fileSeparator = System.getProperty("file.separator")
  override def generateFileNameForKeyValue(key: IntWritable, value: EntityWritable, filename: String): String = {
    EntityMultipleTextFileOutput.generateDirectoryName(key.get) + fileSeparator + filename
  }
}

object EntityMultipleTextFileOutput {
  def generateDirectoryName(entityDescriptionID: Int) = "text_eb_entities_for_ed_" + entityDescriptionID
}