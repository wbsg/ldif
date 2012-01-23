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
  def score(propertyName:String, graph: String) : Double
  def size : Int
  //def asQuads : QuadReader
}

class RandomQualityAssessment extends QualityAssessmentProvider {

  var count = 0

  def score(propertyName:String, graph: String) : Double = {
    count = count + 1
    Math.random
  }

  def size = count

}

class HashBasedQualityAssessment extends RandomQualityAssessment {

}
