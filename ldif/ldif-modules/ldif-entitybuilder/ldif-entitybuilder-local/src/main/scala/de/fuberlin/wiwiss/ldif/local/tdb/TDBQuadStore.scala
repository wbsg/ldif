package de.fuberlin.wiwiss.ldif.local.tdb

import java.io.File
import ldif.local.runtime.EntityWriter
import com.hp.hpl.jena.tdb.TDBFactory
import ldif.util.EntityDescriptionToSparqlConverter
import ldif.entity.{Node, EntityDescription}
import collection.mutable.ArrayBuffer
import scala.collection.JavaConversions._
import com.hp.hpl.jena.rdf.model.RDFNode
import de.fuberlin.wiwiss.ldif.local.{EntityLocalComplete, QuadStoreTrait}
import com.hp.hpl.jena.query.{ResultSet, QuerySolution, QueryExecutionFactory, Dataset}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 07.07.11
 * Time: 15:45
 * To change this template use File | Settings | File Templates.
 */

class TDBQuadStore(tdbRoot: File, databaseRoot: File) extends QuadStoreTrait {
  private var storeStarted = false
  private var dataset: Dataset = null

  private val tempDatabaseDir = createTemporaryDatabaseDirectory(databaseRoot.getCanonicalPath)

  def loadDataset(datasetFile: File) {
    val loader = new TDBLoader

    loader.createNewTDBDatabase(tdbRoot.getCanonicalPath, tempDatabaseDir.getCanonicalPath, datasetFile.getCanonicalPath)
    startStore
  }

  def this(tdbRoot: String, databaseRoot: String) {
    this(new File(tdbRoot), new File(databaseRoot))
  }

  private def startStore = {
    dataset = TDBFactory.createDataset(tempDatabaseDir.getCanonicalPath)
    storeStarted = true
  }

  def clearDatabase {
    storeStarted = false
    dataset = null
    val loader = new TDBLoader
    loader.cleanTarget(tdbRoot.getCanonicalPath, tempDatabaseDir.getCanonicalPath)
  }

  /**
   * Query the store to write entities conforming to the entity
   */
  def queryStore(entityDescription: EntityDescription, entityWriter: EntityWriter): Boolean = {
    if(!storeStarted)
      return false

    val queries = EntityDescriptionToSparqlConverter.convert(entityDescription)
    val nrOfQueries = queries.size


    val resultSets = executeAllQueries(queries)
    val resultManagers = for(resultSet <- resultSets) yield new ResultSetManager(resultSet)
    var entityResults = for(rManager <- resultManagers) yield rManager.getNextEntityData

    while(entityResults.filter(_ != None).size > 0) {
      val entity = ResultSetManager.pickSmallestEntity(entityResults)
      val graph = getGraph(entity, entityResults)
      val factumTable = initFactumTable(nrOfQueries)
      assignResultsForEntity(entity, entityResults, factumTable)
      entityResults = updateEntityResults(entity, entityResults, resultManagers)

      entityWriter.write(EntityLocalComplete(entity, graph, entityDescription, factumTable))
    }

    entityWriter.finish
    return true
  }

  private def getGraph(entityURI: String, entityResults: Seq[Option[EntityData]]): String = {
    for(entityResult <- entityResults)
      if(entityResult!=None && entityResult.get.entityURI==entityURI)
        return entityResult.get.graphURI
    throw new RuntimeException("This should not happen ;)")
  }

  private def updateEntityResults(entityURI: String, entityResults: Seq[Option[EntityData]], resultManagers: Seq[ResultSetManager]): Seq[Option[EntityData]] = {
    return for((entityResult, resultManager) <- entityResults zip resultManagers) yield {
      if(entityResult!=None && entityResult.get.entityURI==entityURI)
        resultManager.getNextEntityData
      else
        entityResult
    }
  }

  private def assignResultsForEntity(entityURI: String, entityResults: Seq[Option[EntityData]], factumTable: Array[Traversable[IndexedSeq[Node]]]) {
    for(i <- 0 to (entityResults.size-1))
      if(entityResults(i)!=None && entityResults(i).get.entityURI==entityURI)
        factumTable(i) = entityResults(i).get.factumTable
  }

  private def initFactumTable(size: Int): Array[Traversable[IndexedSeq[Node]]] = {
    val array = new Array[Traversable[IndexedSeq[Node]]](size)
    for(i <- 0 to (array.size-1))
      array(i) = new ArrayBuffer[IndexedSeq[Node]]
    return array
  }

  private def executeAllQueries(queries: Seq[(String, String)]): Seq[ResultSet] = {
    for(query <- queries) yield QueryExecutionFactory.create(query._1, dataset).execSelect
  }

  def getFactumRow(entity: String, entityGraph: String, resultSet: QuerySolution): IndexedSeq[Node] = {
    val resultVarBaseName = EntityDescriptionToSparqlConverter.resultVarBaseName
    val row = new ArrayBuffer[Node]
    var nrOfVars: Int = 0
    for(v <- resultSet.varNames) nrOfVars += 1
    nrOfVars = (nrOfVars-1) >> 1
    if(nrOfVars==0)
      row.append(Node.createUriNode(entity, entityGraph))
    for(index <- 1 to nrOfVars) {
      val node = resultSet.get(resultVarBaseName + index)
      val graph = resultSet.get(resultVarBaseName + index + "graph")
      row.append(convertNode(node, graph))
    }

    return row
  }

  private def convertLiteralNode(node: RDFNode, graphURI: String): Node = {
    val lexicalValue = node.asLiteral.getLexicalForm
    val datatype = node.asLiteral.getDatatypeURI
    val language = node.asLiteral.getLanguage

    if (datatype != null)
      return Node.createTypedLiteral(lexicalValue, datatype, graphURI)
    else if (language != "")
      return Node.createLanguageLiteral(lexicalValue, language, graphURI)
    else
      return Node.createLiteral(lexicalValue, graphURI)
  }

  def convertNode(node: RDFNode, graph: RDFNode): Node = {
    val graphURI = graph.asResource.getURI
    if(node.isURIResource) {
      return Node.createUriNode(node.asResource.getURI, graphURI)
    } else if(node.isLiteral) {
      return convertLiteralNode(node, graphURI)
    } else if(node.isAnon) {
      return Node.createBlankNode(node.asResource.getId.getLabelString, graphURI)
    } else
      throw new RuntimeException("Unknown node type for RDFNode: " + node) // Should never be the case

  }

  class ResultSetManager(resultSet: ResultSet) {
    var entity: String = null
    var entityGraph: String = null
    var factumTable = new ArrayBuffer[IndexedSeq[Node]]

    def getNextEntityData(): Option[EntityData] = {
      var returnEntityData: EntityData = null

      while(resultSet.hasNext) {
        val querySolution = resultSet.next
        val subj = querySolution.getResource("SUBJ").getURI
        val graph = "test"//querySolution.getResource(graphVar).getURI

        val factumRow = getFactumRow(subj, graph, querySolution)
        if(entity!=subj && entity!=null) {
          returnEntityData = EntityData(entity, entityGraph, factumTable)
          factumTable = new ArrayBuffer[IndexedSeq[Node]]
        }
        entity=subj
        entityGraph=graph
        factumTable.append(factumRow)
        if(returnEntityData != null) {
          return Some(returnEntityData)
        }
      }

      if(entity!=null) {
        if(factumTable.size>0) {
          returnEntityData = EntityData(entity, entityGraph, factumTable)
          factumTable = new ArrayBuffer[IndexedSeq[Node]]
          return Some(returnEntityData)
        }
        else
          return None
      } else
        return None
    }
  }

  object ResultSetManager {
    def pickSmallestEntity(dataOfEntities: Seq[Option[EntityData]]): String = {
      var minEntity: String = dataOfEntities.filter(_ != None).head.get.entityURI
      for(data <- dataOfEntities)
        if(data != None && (data.get.entityURI < minEntity))
          minEntity = data.get.entityURI

      return minEntity
    }
  }

  case class EntityData(val entityURI: String, val graphURI: String, val factumTable: Traversable[IndexedSeq[Node]])
}