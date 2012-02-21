/* 
 * LDIF
 *
 * Copyright 2011-2012 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
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

package ldif.local

import ldif.entity.EntityDescription
import org.slf4j.LoggerFactory
import ldif.util.Uri
import ldif.local.runtime.{ConfigParameters, QuadReader, EntityWriter}
import ldif.runtime.Quad
import java.io._
import java.util.zip.GZIPOutputStream
import runtime.impl.QuadQueue

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 07.07.11
 * Time: 15:02
 * To change this template use File | Settings | File Templates.
 */

class QuadStoreEntityBuilder(store: QuadStoreTrait, entityDescriptions : Seq[EntityDescription], readers : Seq[QuadReader], config: ConfigParameters, reuseDatabase: Boolean = false) extends EntityBuilderTrait {
  private val log = LoggerFactory.getLogger(getClass.getName)

  // If this is true, quads like provenance quads (or even all quads) are saved for later use (merge)
  private val saveQuads = config.otherQuadsWriter!=null
  private val outputAllQuads = config.configProperties.getProperty("output", "mapped-only").toLowerCase=="all"
  private val provenanceGraph = config.configProperties.getProperty("provenanceGraph", "http://www4.wiwiss.fu-berlin.de/ldif/provenance")
  private val tmpDir = new File(config.configProperties.getProperty("databaseLocation", System.getProperty("java.io.tmpdir")))

  private val saveSameAsQuads = config.sameAsWriter!=null
  private val useExternalSameAsLinks = config.configProperties.getProperty("useExternalSameAsLinks", "true").toLowerCase=="true"
  private val outputFormat = config.configProperties.getProperty("outputFormat", "nq").toLowerCase
  private val ignoreProvenance = !(outputFormat=="nq" || outputFormat=="sparql")

  private val PHT = new PropertyHashTable(entityDescriptions)

  initStore
  loadDataset

  private def initStore {
    if(!reuseDatabase)
      store.clearDatabase
  }

  private def loadDataset {
    if(!reuseDatabase) {
      val quadOutput = filterAndDumpDataset(readers)
      store.loadDataset(quadOutput)
    } else
      store.loadDataset(null)
  }

  private def now = System.currentTimeMillis

  // Filter out the relevant quads that will be loaded in to the quad store (also write other quads like provenance somewhere else)
  private def filterAndDumpDataset(readers: Seq[QuadReader]): File = {
    val tempFile = File.createTempFile("quadDump", ".nq.gz", tmpDir)
    tempFile.deleteOnExit
    val writer = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(tempFile)))
//    val writer = new BufferedWriter(new FileWriter(tempFile))
    val startTime = now

    // Round robin over reader
    while (readers.foldLeft(false)((a, b) => a || b.hasNext)){
      for (reader <- readers.filter(_.hasNext)) {
        val quad = reader.read

        if(saveQuads)
          saveQuadsForLater(quad)

        if(useExternalSameAsLinks)
          saveIfSameAsQuad(quad)

        if(isRelevantQuad(quad))
          writer.write(quad.toLine.getBytes)
//          writer.write(quad.toLine)
      }
    }
    writer.flush
    writer.close
    log.info("Created filtered quad file at " + tempFile.getCanonicalPath + " in " + (now - startTime)/1000.0 + "s")

    return tempFile
  }

  private def saveQuadsForLater(quad: Quad) {
    if(outputAllQuads || (isProvenanceQuad(quad) && (!ignoreProvenance)))
      config.otherQuadsWriter.write(quad)
  }

  private def isRelevantQuad(quad: Quad): Boolean = {
    val prop = new Uri(quad.predicate).toString
    if(PHT.contains(prop) && !isProvenanceQuad(quad))
      true
    else
      false
  }

  private def saveIfSameAsQuad(quad: Quad) {
    if(saveSameAsQuads && quad.predicate=="http://www.w3.org/2002/07/owl#sameAs")
      config.sameAsWriter.write(quad)
  }

  private def isProvenanceQuad(quad: Quad): Boolean = {
    if(quad.graph==provenanceGraph)
      true
    else
      false
  }

  def buildEntities(ed: EntityDescription, writer: EntityWriter) = {
    val start = now
    log.info("Starting to build entities...")
    store.queryStore(ed, writer)//TODO: Maybe handle return value
    log.info("Finished building entities in " + (now - start)/1000.0 + "s")
  }

  // TODO to be implemented
  override def getNotUsedQuads : QuadReader = new QuadQueue

}