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

package ldif.hadoop.entitybuilder

import ldif.hadoop.runtime._
import org.apache.hadoop.fs.Path
import org.slf4j.LoggerFactory
import ldif.entity.{EntityDescriptionMetaDataExtractor, EntityDescription}
import ldif.util.Consts
import phases._

class HadoopEntityBuilder (entityDescriptions : IndexedSeq[EntityDescription], readers : Seq[Path], config : ConfigParameters) {

  private val log = LoggerFactory.getLogger(getClass.getName)

  // Build entities
  def buildEntities (writer : Path) {
    val sourcesDir = readers.head.toString
    val hadoopTmpDir = "hadoop_tmp"+Consts.fileSeparator+"output_phase_"+System.currentTimeMillis()

    val edmd = EntityDescriptionMetaDataExtractor.extract(entityDescriptions)

    Phase2.runPhase(sourcesDir, hadoopTmpDir+"_2", edmd, config)
    Phase3.runPhase(hadoopTmpDir+"_2", hadoopTmpDir+"_3", edmd)
    Phase4.runPhase(hadoopTmpDir+"_3", writer.toString, edmd)
  }
}
