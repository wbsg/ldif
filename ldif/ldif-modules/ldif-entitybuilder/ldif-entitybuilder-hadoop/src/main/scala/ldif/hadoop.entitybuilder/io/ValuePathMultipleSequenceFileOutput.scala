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

package ldif.hadoop.entitybuilder.io

import org.apache.hadoop.mapred.lib.MultipleSequenceFileOutputFormat
import org.apache.hadoop.io.IntWritable
import ldif.hadoop.types.{FinishedPathType, ValuePathWritable}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 10/25/11
 * Time: 12:25 PM
 * To change this template use File | Settings | File Templates.
 */

class ValuePathMultipleSequenceFileOutput extends MultipleSequenceFileOutputFormat[IntWritable, ValuePathWritable] {
  val fileSeparator = System.getProperty("file.separator")
  override def generateFileNameForKeyValue(key: IntWritable, value: ValuePathWritable, filename: String): String = {
    var fileName = ""
    if(value.pathType==FinishedPathType)
      fileName = ValuePathMultipleSequenceFileOutput.generateDirectoryNameForFinishedValuePaths(key.get)
    else
      fileName = ValuePathMultipleSequenceFileOutput.generateDirectoryNameForValuePathsInConstruction(key.get)
    fileName + fileSeparator + filename
  }

}

object ValuePathMultipleSequenceFileOutput {
  def generateDirectoryNameForValuePathsInConstruction(phase: Int) = "eb_construct_valuepath_iteration_" + phase
  def generateDirectoryNameForFinishedValuePaths(phase: Int) = "eb_finished_valuepath_iteration_" + phase
}