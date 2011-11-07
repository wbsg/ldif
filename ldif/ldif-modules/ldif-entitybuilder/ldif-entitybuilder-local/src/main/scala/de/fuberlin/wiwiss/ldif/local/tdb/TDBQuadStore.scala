package de.fuberlin.wiwiss.ldif.local.tdb

import java.io.File
import ldif.local.runtime.EntityWriter
import com.hp.hpl.jena.tdb.TDBFactory
import ldif.util.EntityDescriptionToSparqlConverter
import ldif.entity.EntityDescription
import de.fuberlin.wiwiss.ldif.local. QuadStoreTrait
import com.hp.hpl.jena.query.{QueryExecution, ResultSet, QueryExecutionFactory, Dataset}
import ldif.local.util.JenaResultSetEntityBuilderHelper
import com.hp.hpl.jena.query.ARQ

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 07.07.11
 * Time: 15:45
 * To change this template use File | Settings | File Templates.
 */

class TDBQuadStore(databaseRoot: File) extends QuadStoreTrait {
  private var storeStarted = false
  private var dataset: Dataset = null
  ARQ.setTrue(ARQ.spillOnDiskSortingThreshold)
  private val tempDatabaseDir = createTemporaryDatabaseDirectory(databaseRoot.getCanonicalPath)

  def loadDataset(datasetFile: File) {
    val loader = new TDBLoader

    loader.createNewTDBDatabase(tempDatabaseDir.getCanonicalPath, datasetFile.getCanonicalPath)
    startStore
  }

  def this(databaseRoot: String) {
    this(new File(databaseRoot))
  }

  private def startStore = {
    dataset = TDBFactory.createDataset(tempDatabaseDir.getCanonicalPath)
    storeStarted = true
  }

  def clearDatabase {
    storeStarted = false
    dataset = null
    val loader = new TDBLoader
    loader.cleanTarget(tempDatabaseDir.getCanonicalPath)
  }

  /**
   * Query the store to write entities conforming to the entity
   */
  def queryStore(entityDescription: EntityDescription, entityWriter: EntityWriter): Boolean = {
    if(!storeStarted)
      return false

    val queries = EntityDescriptionToSparqlConverter.convert(entityDescription)
    var success = false
    val graphVars = for(query <- queries) yield query._2
    val queryExecutions = getQueryExecutions(queries)
    try {
      val resultSets = executeAllQueries(queryExecutions)
      success = JenaResultSetEntityBuilderHelper.buildEntitiesFromResultSet(resultSets, entityDescription, entityWriter, graphVars)
    } finally {
      queryExecutions.map(_.close)
    }
    return success
  }

  private def getQueryExecutions(queries: Seq[(String, Seq[String])]): Seq[QueryExecution] = {
    for(query <- queries) yield QueryExecutionFactory.create(query._1, dataset)
  }

  private def executeAllQueries(queryExecutions: Seq[QueryExecution]): Seq[ResultSet] = {
    for(queryExecution <- queryExecutions)
      queryExecution.getContext.set(ARQ.spillOnDiskSortingThreshold, 10000l)
    for(queryExecution <- queryExecutions) yield queryExecution.execSelect
  }
}