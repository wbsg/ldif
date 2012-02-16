package ldif.hadoop

import org.apache.hadoop.fs.{Path, FileSystem}
import runtime.ConfigParameters._
import java.math.BigInteger
import java.io.File
import de.fuberlin.wiwiss.r2r._
import runtime.{ConfigParameters, HadoopQuadToTextConverter, RunHadoopURIClustering, RunHadoopQuadConverter}
import scala.collection.JavaConversions._
import ldif.modules.r2r.hadoop.RunHadoopR2RJob
import ldif.hadoop.entitybuilder.HadoopEntityBuilder
import ldif.entity.EntityDescription
import org.apache.hadoop.conf.Configuration
import java.util.Properties
import ldif.util.ConfigProperties

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 2/7/12
 * Time: 6:37 PM
 * To change this template use File | Settings | File Templates.
 */

object R2R {
  def execute(args: Array[String]) {
    if(args.length < 3) {
      sys.error("Parameters: <mappings path> <input path> <output path>")
      sys.exit(1)
    }

    val mappingsPath = args(0)
    val input = args(1)
    val tempDir = "tmp_eb_output"+System.currentTimeMillis
    val output = args(2)

    // remove existing output
    clean(output)

    val mappings = getLdifMappings(mappingsPath)
    val entityDescriptions = getEntityDescriptions(mappings)

    buildEntities(input, tempDir, entityDescriptions)

    RunHadoopR2RJob.execute(tempDir, output, mappings, standalone = true)
    clean(tempDir)
  }

  private def getLdifMappings(mappingsPath: String): IndexedSeq[LDIFMapping] = {
    val mappingSource = new FileOrURISource(new File(mappingsPath))
    val uriGenerator = new EnumeratingURIGenerator("http://www4.wiwiss.fu-berlin.de/ldif/imported", BigInteger.ONE);
    val importedMappingModel = Repository.importMappingDataFromSource(mappingSource, uriGenerator)
    val repository = new Repository(new JenaModelSource(importedMappingModel))
    return (for(mapping <- repository.getMappings.values()) yield LDIFMapping(mapping)).toIndexedSeq
  }

  private def getEntityDescriptions(mappings: IndexedSeq[LDIFMapping]): IndexedSeq[EntityDescription] = {
    (for(mapping <- mappings) yield mapping.entityDescription).toIndexedSeq
  }

  private def buildEntities(input: String, output: String, entityDescriptions: IndexedSeq[EntityDescription]) {
    val properties = {
      if(System.getProperty("ldif.properties", "")!="")
        ConfigProperties.loadProperties(System.getProperty("ldif.properties"))
      else if(new File("ldif.properties").exists())
        ConfigProperties.loadProperties("ldif.properties")
      else
        new Properties()
    }
    val configParameters = ConfigParameters(new Properties(), null, null, null, true)
    val eb = new HadoopEntityBuilder(entityDescriptions, Seq(new Path(input)), configParameters)
    eb.buildEntities(new Path(output))
  }

  // Delete path/directory
  private def clean(hdPath: String) : Path =  {
    val path = new Path(hdPath)
    val hdfs = FileSystem.get(new Configuration())
    if (hdfs.exists(path))
      hdfs.delete(path, true)
    path
  }
}