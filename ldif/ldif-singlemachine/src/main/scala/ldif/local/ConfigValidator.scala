package ldif.local

import datasources.dump.DumpLoader
import de.fuberlin.wiwiss.r2r.{FileOrURISource, Repository}
import scala.collection.mutable.{Map, HashMap}
import scala.collection.JavaConversions._
import java.io.{IOException, BufferedReader, InputStreamReader, File}
import ldif.local.datasources.dump.QuadFileLoader
import collection.JavaConversions

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 05.07.11
 * Time: 15:48
 * To change this template use File | Settings | File Templates.
 */

object ConfigValidator {
  val okMessage = "Ok"
  val fileError = "Error in reading mapping file"
  val mappingsError = "Erroneous mappings found"

  def validateConfiguration(config: LdifConfiguration): Boolean = {
    var fail = false

    try {
      val r2rMappingsErrors = validateMappingFile(config.mappingFile)
      validateSilkLinkSpecs(config.linkSpecDir)
      if(configProperties.getPropertyValue("validate", "true").toLowerCase=="false") {
        println("-- Validation of source datasets disabled")
        return fail
      }
      val sourceFileErrors = validateSourceFiles(config.sourceDir)
    } catch {
      case e: Exception => throw new RuntimeException("Unknown Error occured while validating configuration: " + e.getMessage, e)
    }

    return fail
  }

  def validateSourceFiles(sourceDir: File): Map[String, Seq[Pair[Int, String]]] = {
    val errorMap = new HashMap[String, Seq[Pair[Int, String]]]
    for(file <- sourceDir.listFiles) {
      try {
        val reader = new BufferedReader(new InputStreamReader(new DumpLoader(file.getCanonicalPath).getStream))
        val loader = new QuadFileLoader
        val errors = loader.validateQuads(reader)
        if(errors.size > 0)
          errorMap.put(file.getCanonicalPath, errors)
      } catch {
        case e: IOException => errorMap.put(file.getCanonicalPath, List(Pair(0, "Error reading file: " + e.getMessage)))
      }
    }
    return errorMap
  }

  def validateMappingFile(mappingFile: File): Pair[String, Map[String, String]] = {
    var mappingFileErrors: Pair[String, Map[String, String]] = null

    try {
      val repository = new Repository(new FileOrURISource(mappingFile))
      val erroneousMappings = JavaConversions.asScalaMap(repository.validateMappings)
      if(erroneousMappings.size>0)
        mappingFileErrors = Pair(mappingsError, erroneousMappings)
    } catch {
      case e: Exception => mappingFileErrors = Pair(fileError, null)
    }
    if(mappingFileErrors==null)
      mappingFileErrors = Pair("Ok", null)
    return mappingFileErrors
  }

  def validateSilkLinkSpecs(linkSpecsDir: File) {
     // TODO: Implement
  }
}