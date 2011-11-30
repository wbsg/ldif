package ldif.hadoop.utils

import ldif.hadoop.types.SameAsPairWritable
import ldif.runtime.Quad
import org.apache.hadoop.io.Text
import org.apache.hadoop.mapred.OutputCollector
import ldif.entity.entityComparator.entityComparator

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 11/28/11
 * Time: 4:49 PM
 * To change this template use File | Settings | File Templates.
 */

object URITranslatorHelperMethods {
  def extractAndOutputSameAsPairs(subj: String, obj: String, output: OutputCollector[Text, SameAsPairWritable], iteration: Int, debugOutput: OutputCollector[Text, SameAsPairWritable]) {
    val sameAsPair = new SameAsPairWritable()
    val uri = new Text()
    val from = subj
    val to = obj

    setSameAsPair(sameAsPair, from, to, iteration)
    uri.set(from)
//    debugOutput.collect(uri, sameAsPair) //DEBUG
    output.collect(uri, sameAsPair)

    setSameAsPair(sameAsPair, to, from, iteration)
    uri.set(to)
//    debugOutput.collect(uri, sameAsPair) //DEBUG
    output.collect(uri, sameAsPair)
  }

  private def setSameAsPair(sameAsPair: SameAsPairWritable, from: String, to: String, iteration: Int): SameAsPairWritable = {
    sameAsPair.from = from
    sameAsPair.to = to
    sameAsPair.iteration = iteration
    return sameAsPair
  }

  def simpleCompare(left: String, right: String): Boolean = {
    entityComparator.lessThan(left, right)
  }
}