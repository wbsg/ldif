package ldif.modules.sieve.quality

import java.lang.Math
import collection.mutable.HashMap

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

}

/**
 * Britlle implementation of QAprovider, keeping everything in RAM
 */
class HashBasedQualityAssessment extends QualityAssessmentProvider { //todo implement instead of inherit from random

  var count = 0
  val property2metric = new HashMap[String,Map[String,Double]]()

  def putScore(propertyName: String, graph: String, score: Double) = {
    count = count + 1
    property2metric.put(propertyName, Map(graph -> score) )
  }

  def size = count

  def getScore(propertyName: String, graph: String) = {
    property2metric.get(propertyName) match {
      case Some(metric) => metric.getOrElse(graph,0.0)
      case None => 0.0
    }
  }

}
