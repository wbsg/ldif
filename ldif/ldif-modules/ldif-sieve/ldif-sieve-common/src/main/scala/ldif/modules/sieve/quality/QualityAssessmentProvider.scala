package ldif.modules.sieve.quality

import java.lang.Math
import collection.mutable.HashMap
import sun.reflect.generics.reflectiveObjects.NotImplementedException

/**
 * Contains scored graphs for each indicator.
 * A simple implementation contains an index of indicators or graph IDs
 * schema
 *   { indicator: { graphId: score }
 * example
 *   { "sieve:lastUpdated": { "en.wikipedia.org": 0.98, "pt.dbpedia.org": 0.8 }
 * @author pablomendes
 */
trait QualityAssessmentProvider {
  def getScore(propertyName:String, graph: String) : Double
  def size : Int
  //def asQuads : QuadReader
  def putScore(propertyName:String, graph: String, score:Double)

  /**
   * Given a metricId, returns scored graphs in the form of a Map( graphId: String -> score: Double )
   */
  //def getScoredGraphs(propertyName: String): Map[String,Double]

}


class RandomQualityAssessment extends QualityAssessmentProvider {

  var count = 0

  def getScore(propertyName:String, graph: String) : Double = {
    Math.random
  }

  def size = count

  def putScore(propertyName: String, graph: String, score: Double) = {
    count = count + 1
  }

//  def getScoredGraphs(propertyName: String) = {
//    throw new NotImplementedException //todo implement
//  }
}

/**
 * Britlle implementation of QAprovider, keeping everything in RAM
 */
class HashBasedQualityAssessment extends QualityAssessmentProvider { //todo implement instead of inherit from random

  var count = 0
  val property2metric = new HashMap[String,Map[String,Double]]()

  def putScore(propertyName: String, graph: String, score: Double) {
    assume (score <= 1.0)
    if (score>0.0) { // store as sparse matrix. if no hit is found, score is 0.0
      count = count + 1
      val scoredGraphs : Map[String,Double] = property2metric.getOrElse(propertyName, Map[String,Double]()) ++ Map(graph -> score)
      property2metric.put(propertyName, scoredGraphs)
    }
  }

  def size = count

  def getScore(propertyName: String, graph: String) = {
    property2metric.get(propertyName) match {
      case Some(metric) => metric.getOrElse(graph,0.0)
      case None => 0.0
    }
  }

//  def getScoredGraphs(propertyName: String) = {
//    property2metric.getOrElse(propertyName, Map[String,Double]())
//  }

}
