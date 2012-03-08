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
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 07.07.11
 * Time: 15:02
 * To change this template use File | Settings | File Templates.
 */

class QuadStoreEntityBuilder(store: QuadStoreTrait, entityDescriptions : Seq[EntityDescription], readers : Seq[QuadReader], config: ConfigParameters, reuseDatabase: Boolean = false) extends EntityBuilderTrait {
  private val log = LoggerFactory.getLogger(getClass.getName)
  private val quadCounter = new AtomicInteger(0)

  private val tmpDir = new File(config.configProperties.getProperty("databaseLocation", System.getProperty("java.io.tmpdir")))

  private val PHT = new PropertyHashTable(entityDescriptions)
  entityBuilderReportPublisher.name = "Entity Builder (quad store)"

  initStore
  loadDataset

  private def initStore {
    if(!reuseDatabase) {
      store.clearDatabase
      quadCounter.set(0)
    }
  }

  private def loadDataset {
    if(!reuseDatabase) {
      val quadOutput = filterAndDumpDataset(readers, quadCounter)
      store.loadDataset(quadOutput)
      entityBuilderReportPublisher.quadsReadCounter.set(quadCounter.get)
      entityBuilderReportPublisher.finishedReading=true
    } else
      store.loadDataset(null)
  }

  private def now = System.currentTimeMillis

  // Filter out the relevant quads that will be loaded in to the quad store (also write other quads like provenance somewhere else)
  private def filterAndDumpDataset(readers: Seq[QuadReader], counter: AtomicInteger = null): File = {
    val compress = config.configProperties.getProperty("compressFilteredDataset", "false").toLowerCase=="true"
    val suffix = if(compress) ".gz" else ""
    val tempFile = File.createTempFile("quadDump", ".nq"+suffix, tmpDir)
    tempFile.deleteOnExit

    val writer = if(compress)
        new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(tempFile)))
      else
        new BufferedOutputStream(new FileOutputStream(tempFile))

    val startTime = now

    // Round robin over reader
    while (readers.foldLeft(false)((a, b) => a || b.hasNext)){
      for (reader <- readers.filter(_.hasNext)) {
        val quad = reader.read

        if(isRelevantQuad(quad)) {
          writer.write(quad.toLine.getBytes)
          if(counter!=null)
            counter.incrementAndGet()
        }
//          writer.write(quad.toLine)
      }
    }
    writer.flush
    writer.close
    log.info("Created filtered quad file at " + tempFile.getCanonicalPath + " in " + (now - startTime)/1000.0 + "s")

    return tempFile
  }

  private def isRelevantQuad(quad: Quad): Boolean = {
    val prop = new Uri(quad.predicate).toString
    if(PHT.contains(prop))
      true
    else
      false
  }

  def buildEntities(ed: EntityDescription, writer: EntityWriter) = {
    val start = now
    val entityCounter = new AtomicInteger(0)
    log.info("Starting to build entities...")
    store.queryStore(ed, writer, entityCounter)//TODO: Maybe handle return value
    entityBuilderReportPublisher.entitiesBuilt.addAndGet(entityCounter.get)
    entityBuilderReportPublisher.entityQueuesFilled.incrementAndGet()
    log.info("Finished building " + entityCounter + " entities in " + (now - start)/1000.0 + "s")
  }

  // TODO to be implemented
  override def getNotUsedQuads : QuadReader = new QuadQueue

}