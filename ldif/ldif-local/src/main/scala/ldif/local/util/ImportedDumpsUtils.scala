package ldif.local.util

import ldif.local.scheduler.ImportJob
import ldif.datasources.dump.QuadParser
import java.io.File
import org.slf4j.LoggerFactory
import java.util.Calendar
import ldif.util.{CommonUtils, Identifier, Consts}

/**
 *  Imported dumps utilities
 */

case class ImportedDumpsUtils(dumpsLocation : String) {

  private val log = LoggerFactory.getLogger(getClass.getName)
  private val dumpsDir = new File(dumpsLocation)

  /* Retrieve last update for a given import (from provenance info) */
  def getLastUpdate(job : ImportJob) : Calendar = getLastUpdate(job.id)

  def getLastUpdate(jobId : Identifier) : Calendar = {
    val provenanceFile = getProvenanceFile(jobId)
    if (provenanceFile.exists) {
      val lines = scala.io.Source.fromFile(provenanceFile).getLines
      val parser = new QuadParser
      // loop and stop as the first lastUpdate quad is found
      for (quad <- lines.toTraversable.map(parser.parseLine(_))){
        if (quad.predicate.equals(Consts.lastUpdateProp))  {
          val lastUpdateStr = quad.value.value
          if (lastUpdateStr.length != 25)  {
            log.warn("Job "+jobId+" - wrong datetime format for last update metadata")
            return null
          }
          else {
            val sb = new StringBuilder(lastUpdateStr).deleteCharAt(22)
            val lastUpdateDate = Consts.xsdDateTimeFormat.parse(sb.toString)
            return CommonUtils.dateToCalendar(lastUpdateDate)
          }
        }
      }
      log.warn("Job "+jobId+" - provenance file does not contain last update metadata")
      null
    }
    else {
      //log.warn("Provenance file not found at "+provenanceFile.getCanonicalPath)
      null
    }
  }

  /* Retrieve the number of imported quads for a given import (from provenance info) */
  def getNumberOfQuads(job : ImportJob) : Option[Double] = getNumberOfQuads(job.id)

  def getNumberOfQuads(jobId : Identifier) : Option[Double] = getNumberOfQuads(getProvenanceFile(jobId))

  def getNumberOfQuads(provenanceFile : File) : Option[Double] = {
    if (provenanceFile.exists) {
      val lines = scala.io.Source.fromFile(provenanceFile).getLines
      val parser = new QuadParser
      // loop and stop as the first numberOfQuads property is found
      for (quad <- lines.toTraversable.map(parser.parseLine(_))){
        if (quad.predicate.equals(Consts.importedQuadsProp)){
          return Some(quad.value.value.toDouble)
        }
      }
      log.warn("Provenance file does not contain last update metadata: "+ provenanceFile.getCanonicalPath)
      None
    }
    else {
      //log.warn("Provenance file not found at "+provenanceFile.getCanonicalPath)
      None
    }
  }

  /* Retrieve the number of imported quads for the given imports (from provenance info) */
  def getNumberOfQuads(jobs : Traversable[ImportJob]) : Double =
    jobs.map(getNumberOfQuads(_).get).sum

  /* Retrieve the number of imported quads for all dumps in dumpsDir (from provenance info) */
  def getNumberOfQuads : Double = {
    val provenanceFiles = CommonUtils.listFiles(dumpsDir, "provenance.nq")
    provenanceFiles.map(getNumberOfQuads(_).get).sum
  }

  private def getProvenanceFile(jobId : Identifier) : File = new File(dumpsDir + "/"+ jobId +".provenance.nq")





}