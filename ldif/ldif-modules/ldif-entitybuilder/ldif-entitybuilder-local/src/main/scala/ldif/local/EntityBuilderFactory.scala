package ldif.local

import ldif.local.runtime.{ConfigParameters, QuadReader}
import ldif.entity.EntityDescription
import tdb.TDBQuadStore

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 07.07.11
 * Time: 17:21
 * To change this template use File | Settings | File Templates.
 */

object EntityBuilderFactory {
  def getEntityBuilder(configParameters: ConfigParameters, entityDescriptions: IndexedSeq[EntityDescription], reader : Seq[QuadReader]): EntityBuilderTrait = {
    val entityBuilderType = configParameters.configProperties.getProperty("entityBuilderType", "in-memory").toLowerCase
    entityBuilderType match {
      case "voldemort" => new EntityBuilder(entityDescriptions, reader, configParameters)
      case "in-memory" => new EntityBuilder(entityDescriptions, reader, configParameters)
      case "quad-store" => {
        createQuadStoreEntityBuilder(configParameters, entityDescriptions, reader)
      }
    }
  }

  private def createQuadStore(quadStoreType: String, databaseLocation: String): QuadStoreTrait = {
    quadStoreType match {
      case "tdb" => {
        new TDBQuadStore(databaseLocation)
      }
      case _ => throw new RuntimeException("Unknown quad store type: " + quadStoreType)
    }
  }

  private def createQuadStoreEntityBuilder(configParameters: ConfigParameters, entityDescriptions: scala.Seq[EntityDescription], reader: scala.Seq[QuadReader]): EntityBuilderTrait = {
    val quadStoreType = configParameters.configProperties.getProperty("quadStoreType", "tdb").toLowerCase
    val databaseLocation = configParameters.configProperties.getProperty("databaseLocation", System.getProperty("java.io.tmpdir"))
    val quadStore = createQuadStore(quadStoreType, databaseLocation)
    new QuadStoreEntityBuilder(quadStore, entityDescriptions, reader, configParameters)
  }
}