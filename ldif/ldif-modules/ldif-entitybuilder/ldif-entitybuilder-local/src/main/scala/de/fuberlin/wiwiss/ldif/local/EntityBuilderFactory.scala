package de.fuberlin.wiwiss.ldif.local

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
    val entityBuilderType = configParameters.configProperties.getPropertyValue("entityBuilderType", "in-memory").toLowerCase
    entityBuilderType match {
      case "voldemort" => return new EntityBuilder(entityDescriptions, reader, configParameters)
      case "in-memory" => return new EntityBuilder(entityDescriptions, reader, configParameters)
      case "quad-store" => {
        return createQuadStoreEntityBuilder(configParameters, entityDescriptions, reader)
      }
    }
  }

  private def createQuadStore(quadStoreType: String, configParameters: ConfigParameters, databaseLocation: String): QuadStoreTrait = {
    quadStoreType match {
      case "tdb" => {
        val tdbRoot = configParameters.configProperties.getPropertyValue("tdbRoot", null)
        if (tdbRoot == null)
          throw new RuntimeException("tdbRoot property not specified!")
        new TDBQuadStore(tdbRoot, databaseLocation)
      }
      case _ => throw new RuntimeException("Unknown quad store type: " + quadStoreType)
    }
  }

  private def createQuadStoreEntityBuilder(configParameters: ConfigParameters, entityDescriptions: scala.Seq[EntityDescription], reader: scala.Seq[QuadReader]): EntityBuilderTrait = {
    val quadStoreType = configParameters.configProperties.getPropertyValue("quadStoreType", "tdb").toLowerCase
    val databaseLocation = configParameters.configProperties.getPropertyValue("databaseLocation", System.getProperty("java.io.tmpdir"))
    val quadStore = createQuadStore(quadStoreType, configParameters, databaseLocation)
    return new QuadStoreEntityBuilder(quadStore, entityDescriptions, reader, configParameters)
  }
}