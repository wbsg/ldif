package ldif.modules.sieve.quality

import java.lang.Math

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

class HashBasedQualityAssessment extends RandomQualityAssessment { //todo implement instead of inherit from random

}
