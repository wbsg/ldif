package ldif.local

import datasources.dump.DumpLoader
import de.fuberlin.wiwiss.r2r.{FileOrURISource, Repository}
import scala.collection.mutable.{Map, HashMap}
import java.io.{IOException, BufferedReader, InputStreamReader, File}
import ldif.local.datasources.dump.QuadFileLoader
import collection.JavaConversions

object ConfigValidator {
  val okMessage = "Ok"
  val fileError = "Error in reading mapping file"
  val mappingsError = "Erroneous mappings found"

  def validateConfiguration(config: LdifConfiguration): Boolean = {
    var fail = false

    try {
      val r2rMappingsErrors = validateMappingFile(config.mappingFile)
      if(r2rMappingsErrors._1!="Ok")
        fail = true
      validateSilkLinkSpecs(config.linkSpecDir)
      if(configProperties.getPropertyValue("validate", "true").toLowerCase=="false") {
        println("-- Validation of source datasets disabled")
        return fail
      }
      val sourceFileErrors = validateSourceFiles(config.sources)
      for (err <- sourceFileErrors)   {
        println("!- Error(s) found in source: "+ err._1)
        fail = true
      }
    } catch {
      case e: Exception => throw new RuntimeException("Unknown Error occured while validating configuration: " + e.getMessage, e)
    }
    return fail
  }

  def validateSourceFiles(sources: Traversable[String]): Map[String, Seq[Pair[Int, String]]] = {
    val errorMap = new HashMap[String, Seq[Pair[Int, String]]]
    for(source <- sources) {
      try {
        val reader = new BufferedReader(new InputStreamReader(new DumpLoader(source).getStream))
        val loader = new QuadFileLoader
        val errors = loader.validateQuads(reader)
        if(errors.size > 0)
          errorMap.put(source, errors)
      } catch {
        case e: IOException => errorMap.put(source, List(Pair(0, "Error reading file: " + e.getMessage)))
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