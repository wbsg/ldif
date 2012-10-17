package ldif.wikidata.model

import ldif.entity.Node
import ldif.entity.Node._
import ldif.runtime.{Quad, Triple, QuadWriter}
import ldif.util.Consts
import ldif.wikidata.util.WikidataConsts._

/**
 * Created with IntelliJ IDEA.
 * User: andreas
 * Date: 10/9/12
 * Time: 11:02 AM
 * To change this template use File | Settings | File Templates.
 */
case class WikidataNode(val value: Node, var children: List[WikidataNode] = List(), var parent: Option[WikidataNode] = None, var propertyToParent: Option[String] = None) {
  def serializeWikidataOutput(writer: QuadWriter) {
    generateTriple match {
      case Some(quad) => writer.write(quad)
      case _ =>
    }
    for(child <- children)
      child.serializeWikidataOutput(writer)
  }

  def serializeLDIFOutput(writer: QuadWriter, graph: String) {
    generateQuad(graph) match {
      case Some(quad) => writer.write(quad)
      case _ =>
    }
    for(child <- children)
      child.serializeLDIFOutput(writer, graph)
  }

  def generateTriple: Option[Quad] = (parent, propertyToParent) match {
    case (Some(par), Some(prop)) => Some(Triple(par.value, prop, value))
    case _ => None
  }

  def getItem: WikidataNode = parent match {
    case None => this
    case Some(p) => p.getItem
  }

  def generateQuad(graph: String): Option[Quad] = (parent, propertyToParent) match {
    case (Some(par), Some(prop)) => if (prop.startsWith(wikidataV))
        Some(Quad(getItem.value, prop, value, graph))
      else if (prop.startsWith(wikidataQ))
        Some(Quad(Node.createUriNode(graph, Consts.DEFAULT_PROVENANCE_GRAPH), prop, value, Consts.DEFAULT_PROVENANCE_GRAPH))
      else
        None
    case _ => None
  }
}

case class WikidataStatement(val item: WikidataNode) {
  lazy val graph = item.children match {
    case statementObj :: _ => Consts.DEFAULT_PROVENANCE_GRAPH + "/" + statementObj.value.value.substring(wikidataS.length)
    case _ => throw new RuntimeException("No valid Wikidata statement: " + this.toString)
  }

  def serializeWikidataOutput(writer: QuadWriter) { item.serializeWikidataOutput(writer)}
}
