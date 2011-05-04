package de.fuberlin.wiwiss.r2r

import de.fuberlin.wiwiss.r2r.parser._
import ldif.entity._
import scala.collection.JavaConversions._

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 03.05.11
 * Time: 19:23
 * To change this template use File | Settings | File Templates.
 */

class SourcePatternToEntityDescriptionTransformer {
  def main(args: Array[String]) : Unit = {
    transform("?s <gsdsdf> [ <nsprop> 'blah' ; <fsdfsd> <fsdf>; <fdsdfs> [<fsdfsd> 'fsds' ;<p4> <fd>]]")
  }

  def transform(sourcePattern: String): EntityDescription = {
    val triples: List[NodeTriple] =  Sparql2NodeTripleParser.parse(sourcePattern).toList
    triples.foreach(arg => println(arg))
    return null
  }
}