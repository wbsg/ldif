package de.fuberlin.wiwiss.ldif.local.tdb

import java.io.File
import ldif.local.runtime.EntityWriter
import com.hp.hpl.jena.tdb.TDBFactory
import ldif.util.EntityDescriptionToSparqlConverter
import ldif.entity.{Node, EntityDescription}
import collection.mutable.ArrayBuffer
import com.hp.hpl.jena.query.{QuerySolution, QueryExecutionFactory, Dataset}
import scala.collection.JavaConversions._
import com.hp.hpl.jena.rdf.model.RDFNode
import de.fuberlin.wiwiss.ldif.local.{EntityLocalComplete, QuadStoreTrait}

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

    val query = EntityDescriptionToSparqlConverter.convert(entityDescription)

    val queryExecution = QueryExecutionFactory.create(query.head._1, dataset)
    val graphVar = query.head._2
    val resultSet = queryExecution.execSelect()

    var entity: String = null
    var entityGraph: String = null
    var factumTable = new ArrayBuffer[IndexedSeq[Node]]

    while(resultSet.hasNext) {
      val querySolution = resultSet.next
      val subj = querySolution.getResource("SUBJ").getURI
      val graph = querySolution.getResource(graphVar).getURI
      val factumRow = getFactumRow(subj, graph, querySolution)
      if(entity!=subj && entity!=null) {
        entityWriter.write(EntityLocalComplete(entity, entityGraph, entityDescription, ArrayBuffer(factumTable)))
        factumTable = new ArrayBuffer[IndexedSeq[Node]]
      }
      entity=subj
      entityGraph=graph
      factumTable.append(factumRow)
    }

    if(entity!=null) {
       entityWriter.write(EntityLocalComplete(entity, entityGraph, entityDescription, ArrayBuffer(factumTable)))
    }


    entityWriter.finish
    return true
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

  def convertNode(node: RDFNode, graph: RDFNode): Node = {
    val graphURI = graph.asResource.getURI
    if(node.isURIResource) {
      val nodeURI = node.asResource.getURI
      return Node.createUriNode(nodeURI, graphURI)
    } else if(node.isLiteral) {
      val lexicalValue = node.asLiteral.getLexicalForm
      val datatype = node.asLiteral.getDatatypeURI
      val language = node.asLiteral.getLanguage

      if(datatype!=null)
        return Node.createTypedLiteral(lexicalValue, datatype, graphURI)
      else if(language!="")
        return Node.createLanguageLiteral(lexicalValue, language, graphURI)
      else
        Node.createLiteral(lexicalValue, graphURI)
    } else
      null//TODO: Handle Blank Nodes etc.

  }
}