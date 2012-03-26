package ldif.hadoop

import ldif.entity.EntityDescription
import ldif.util.ConfigProperties
import java.io.File
import java.util.Properties
import runtime.ConfigParameters
import ldif.hadoop.entitybuilder.HadoopEntityBuilder
import org.apache.hadoop.fs.Path

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 3/26/12
 * Time: 2:43 PM
 * To change this template use File | Settings | File Templates.
 */

object EB {
  def buildEntities(input: String, output: String, entityDescriptions: IndexedSeq[EntityDescription]) {
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
}