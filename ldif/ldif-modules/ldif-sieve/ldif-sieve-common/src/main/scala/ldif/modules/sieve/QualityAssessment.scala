package ldif.modules.sieve

/**
 * Contains scored graphs for each indicator.
 * A simple implementation contains an index of indicators or graph IDs
 * schema: { indicator: { graphId: score }
 * example: { "sieve:lastUpdated": { "en.wikipedia.org": 0.98, "pt.dbpedia.org": 0.8 }
 * @author pablomendes
 */
class QualityAssessment {


  def score(propertyName:String, graph: String) : Double = {
    Math.random
  }
}