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

import datasources.dump.DumpLoader
import de.fuberlin.wiwiss.r2r.{FileOrURISource, Repository}
import scala.collection.mutable.{Map, HashMap}
import ldif.local.datasources.dump.QuadFileLoader
import collection.JavaConversions
import org.slf4j.LoggerFactory
import java.io._
import ldif.config.IntegrationConfig

object ConfigValidator {
  val okMessage = "Ok"
  val log = LoggerFactory.getLogger(getClass.getName)
  val fileError = "Error in reading mapping file"
  val mappingsError = "Erroneous mappings found"

  def validateConfiguration(config: IntegrationConfig) : Boolean = {
    var fail = false
    val skipSilk = config.properties.getProperty("linkSpecifications.skip", "false")=="true"
    val skipR2R = config.properties.getProperty("mappings.skip", "false")=="true"

    try {
      val r2rMappingsErrors = validateMappingFile(config.mappingDir, skipR2R)
      var sourceFileErrors : Map[String, Seq[Pair[Int, String]]] = null
      if(r2rMappingsErrors._1!="Ok")
        fail = true
      validateSilkLinkSpecs(config.linkSpecDir, skipSilk)

      val sourceValidation = config.properties.getProperty("validateSources")
      val discardFaultyQuads = config.properties.getProperty("discardFaultyQuads", "false").toLowerCase=="true"
      if(sourceValidation!=null && sourceValidation.toLowerCase=="false") {
        log.debug("Validation of source datasets disabled")
      }
      else {
        // Sources validation
         sourceFileErrors = validateSourceFiles(config.sources)

        for (err <- sourceFileErrors) {
          if(err._2.size > 0 && !discardFaultyQuads)
            fail = true
        }
      }
      logErrors(r2rMappingsErrors, sourceFileErrors)
    } catch {
      case e: Exception => throw new RuntimeException("Unknown Error occured while validating configuration: " + e.getMessage, e)
    }

    fail
  }

  def logErrors(r2rMappingErrors: (String, Map[String, String]), sourceFileErrors: Map[String, Seq[Pair[Int, String]]]) {
    if(r2rMappingErrors!=null)
      logR2RErrors(r2rMappingErrors)
    if(sourceFileErrors!=null)
      logSourceFileErrors(sourceFileErrors)
  }

  private def logR2RErrors(r2rMappingErrors: (String, Map[String, String])) {
    if(r2rMappingErrors._1=="Ok")
      return

    log.warn("Found R2R errors in configuration: " + r2rMappingErrors._1)
    if(r2rMappingErrors._2 != null)
      for((mapping, errorString) <- r2rMappingErrors._2)
        log.warn("Mapping <" + mapping + "> contains an error: " + errorString)
  }

  private def logSourceFileErrors(sourceErrors: Map[String, Seq[Pair[Int, String]]]) {
    for((source, errorMap) <- sourceErrors) {
      if(errorMap.size > 0) {
        val errorString = new StringBuilder()
        errorString.append("There have been ").append(errorMap.size).append(" errors in input source ").append(source).append(":")
        for((lineNr, line) <- errorMap)
          errorString.append("\n  ").append("In line ").append(lineNr).append(": ").append(line)
        log.warn(errorString.toString)
      }
    }
  }

  // Validates a list of files/directories
  def validateSourceFiles(sources: Traversable[String]): Map[String, Seq[Pair[Int, String]]] = {
    val errorMap = new HashMap[String, Seq[Pair[Int, String]]]
    for(source <- sources) {
      val sourceFile = new File(source)
      if(sourceFile.isDirectory)
        sourceFile.listFiles().filterNot(_.isHidden).map(validateSourceFile(_,errorMap))
      else
        validateSourceFile(sourceFile,errorMap)
    }
    errorMap
  }

  // Validates a file
  def validateSourceFile(file : File, errorMap : Map[String, Seq[Pair[Int, String]]]) {
    try {
      val reader = new BufferedReader(new InputStreamReader(DumpLoader.getFileStream(file)))
      val loader = new QuadFileLoader
      val errors = loader.validateQuadsMT(reader)
      if(errors.size > 0)
        errorMap.put(file.getCanonicalPath, errors)
    } catch {
      case e: IOException => errorMap.put(file.getCanonicalPath, List(Pair(0, "Error reading file: " + e.getMessage)))
    }

  }


  def validateMappingFile(mappingFile: File, skip: Boolean): Pair[String, Map[String, String]] = {
    if(skip)
      return ("Ok", null)
    var mappingFileErrors: Pair[String, Map[String, String]] = null

    try {
      val repository = new Repository(new FileOrURISource(mappingFile))
      val erroneousMappings = JavaConversions.mapAsScalaMap(repository.validateMappings)
      if(erroneousMappings.size>0)
        mappingFileErrors = Pair(mappingsError, erroneousMappings)
    } catch {
      case e: Exception => mappingFileErrors = Pair(fileError + ": " + e.getMessage, null)
    }
    if(mappingFileErrors==null)
      mappingFileErrors = Pair("Ok", null)
    return mappingFileErrors
  }

  def validateSilkLinkSpecs(linkSpecsDir: File, skip: Boolean) {
    // TODO: Implement
  }
}