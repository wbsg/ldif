import collection.mutable
import de.fuberlin.wiwiss.silk.plugins.metric.SubStringDistance
import de.fuberlin.wiwiss.silk.plugins.transformer.StripUriPrefixTransformer
import ldif.local.datasources.dump.DumpLoader
import ldif.local.runtime.impl.FileQuadReader

object StreamMatcher {
  def main(args: Array[String]) {
    if(args.length<2) {
      println("Parameters: <source entities> <target entities>")
      sys.exit(-1)
    }
    val source = DumpLoader.dumpIntoFileQuadQueue(args(0))
    val target = DumpLoader.dumpIntoFileQuadQueue(args(1))
    val substring = SubStringDistance()

    val stripURIprefix = StripUriPrefixTransformer()
    val nGramMap = createNGramIndex(target, stripURIprefix)
    for(sourceQuad <- source) {
      val sourceURI = sourceQuad.subject.value
      val sourceLabel = sourceQuad.value.value
      val targetCandidates = fetchTargetCandidates(sourceLabel, nGramMap)
      for(candidate <- targetCandidates; score=substring.evaluate(sourceLabel, candidate) if score > 0.05 && score <=0.1)
        println(sourceURI + " " + candidate)
    }
  }

  private def fetchTargetCandidates(label: String, nGramMap: Map[String, mutable.Set[String]]): Set[String] = {
    val candidates = for(nGram <- SubStringDistance.getNgrams(SubStringDistance.normalizeString(label.toLowerCase), 3))
      yield nGramMap.getOrElse(nGram, mutable.Set[String]())
    candidates.flatten.toSet
  }

  private def createNGramIndex(target: FileQuadReader, stripURIprefix: StripUriPrefixTransformer): Map[String, mutable.Set[String]] = {
    val nGramMap = new mutable.HashMap[String, mutable.Set[String]]()
    for (targetQuad <- target) {
      val uri = targetQuad.subject.value
      val label = stripURIprefix.evaluate(uri)
      for (nGram <- SubStringDistance.getNgrams(SubStringDistance.normalizeString(label.toLowerCase), 3)) {
        val labelSet = nGramMap.getOrElse(nGram, mutable.HashSet[String]())
        labelSet.add(label)
        nGramMap.put(nGram, labelSet)
      }

    }
    nGramMap.toMap
  }
}

class SubstringMatcher {
  private val nGramMap = new mutable.HashMap[String, mutable.Set[String]]()
  private val stripURIprefix = StripUriPrefixTransformer()
  private val substring = SubStringDistance()

  def addTargetURI(uri: String) {
    val label = stripURIprefix.evaluate(uri)
    for (nGram <- SubStringDistance.getNgrams(SubStringDistance.normalizeString(label.toLowerCase), 3)) {
      val labelSet = nGramMap.getOrElseUpdate(nGram, mutable.HashSet[String]())
      labelSet.add(uri)
    }
  }

  def matchURI(uri: String, threshold: Double): List[(Double,String)] = {
    val label = stripURIprefix.evaluate(uri)
    val candidates = fetchTargetCandidates(label)
    (for(candidate <- candidates; score=substring.evaluate(label, stripURIprefix.evaluate(candidate)) if score <= threshold)
      yield (score, candidate)).toList.sortWith(_._1 < _._1)
  }

  private def fetchTargetCandidates(label: String): Set[String] = {
    val candidates = for(nGram <- SubStringDistance.getNgrams(SubStringDistance.normalizeString(label.toLowerCase), 3))
    yield nGramMap.getOrElse(nGram, mutable.Set[String]())
    candidates.flatten.toSet
  }
}