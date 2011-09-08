package de.fuberlin.wiwiss.ldif.local

import ldif.entity.EntityDescription
import java.util.logging.Logger
import ldif.util.Uri
import ldif.local.runtime.{ConfigParameters, QuadReader, EntityWriter}
import java.io.{BufferedWriter, FileWriter, IOException, File}
import ldif.runtime.Quad

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 07.07.11
 * Time: 15:02
 * To change this template use File | Settings | File Templates.
 */

class QuadStoreEntityBuilder(store: QuadStoreTrait, entityDescriptions : Seq[EntityDescription], readers : Seq[QuadReader], config: ConfigParameters) extends EntityBuilderTrait {
  private val log = Logger.getLogger(getClass.getName)

  // If this is true, quads like provenance quads (or even all quads) are saved for later use (merge)
  private val saveQuads = config.otherQuadsWriter!=null
  private val outputAllQuads = config.configProperties.getPropertyValue("output", "mapped-only").toLowerCase=="all"
  private val provenanceGraph = config.configProperties.getPropertyValue("provenanceGraph", "http://www4.wiwiss.fu-berlin.de/ldif/provenance")
  private val tmpDir = new File(config.configProperties.getPropertyValue("databaseLocation", System.getProperty("java.io.tmpdir")))

  private val PHT = new PropertyHashTable(entityDescriptions)

  initStore
  loadDataset

  private def initStore {
    store.clearDatabase
  }

  private def loadDataset {
    val quadOutput = filterAndDumpDataset(readers)
    store.loadDataset(quadOutput)
  }

  private def now = System.currentTimeMillis

  // Filter out the relevant quads that will be loaded in to the quad store (also write other quads like provenance somewhere else)
  private def filterAndDumpDataset(readers: Seq[QuadReader]): File = {
    val tempFile = File.createTempFile("quadDump", ".nq", tmpDir)
    tempFile.deleteOnExit
    val writer = new BufferedWriter(new FileWriter(tempFile))
    val startTime = now

    // Round robin over reader
    while (readers.foldLeft(false)((a, b) => a || b.hasNext)){
      for (reader <- readers.filter(_.hasNext)) {
        val quad = reader.read

        if(saveQuads)
          saveQuadsForLater(quad)

        val prop = new Uri(quad.predicate).toString

        PHT.get(prop) match {
          case Some(_) => writer.write(quad.toNQuadFormat); writer.write(" . \n")
          case None => //do nothing
        }
      }
    }
    writer.flush
    writer.close
    log.info("Created filtered quad file at " + tempFile.getCanonicalPath + " in " + (now - startTime)/1000.0 + "s")

    return tempFile
  }

  private def saveQuadsForLater(quad: Quad) {
    if(outputAllQuads || isProvenanceQuad(quad))
      config.otherQuadsWriter.write(quad)
  }

  private def isRelevantQuad(quad: Quad): Boolean = {
    val prop = new Uri(quad.predicate).toString
    if(PHT.contains(prop) && !isProvenanceQuad(quad))
      true
    else
      false
  }

  private def isProvenanceQuad(quad: Quad): Boolean = {
    if(quad.graph==provenanceGraph)
      true
    else
      false
  }

  def buildEntities(ed: EntityDescription, writer: EntityWriter) = {
    val start = now
    store.queryStore(ed, writer)//TODO: Maybe handle return value
  }
}