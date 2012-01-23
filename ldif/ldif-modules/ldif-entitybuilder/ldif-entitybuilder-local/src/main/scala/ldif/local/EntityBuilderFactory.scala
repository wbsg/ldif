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