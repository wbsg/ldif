package ldif.local.runtime

import ldif.entity.Node
import ldif.local.util.StringPool

/*
 * Object used to create Node using string canonicalization
 */

object LocalNode
{
  private var useStringPool = true

  def createLiteral(value : String, graph : String) = new Node(strCan(value), null, Node.Literal, strCan(graph))

  def createTypedLiteral(value : String, datatype : String, graph : String) = new Node(strCan(value), strCan(datatype), Node.TypedLiteral, strCan(graph))

  def createLanguageLiteral(value : String, language : String, graph : String) = new Node(strCan(value), strCan(language), Node.LanguageLiteral, strCan(graph))

  def createBlankNode(value : String, graph : String) = new Node(strCan(value), null, Node.BlankNode, strCan(graph))

  def createUriNode(value : String, graph : String) = new Node(strCan(value), null, Node.UriNode, strCan(graph))

  def fromNxNode(nxNode : org.semanticweb.yars.nx.Node, graph : String) = {
    nxNode match {
      case lit:org.semanticweb.yars.nx.Literal => {
        val dt = lit.getDatatype
        val lang = lit.getLanguageTag
        val value = strCan(lit.getData)
        if (dt!=null)
          ldif.entity.Node.createTypedLiteral(value,strCan(dt.toString),graph)
        else if (lang!=null)
          ldif.entity.Node.createLanguageLiteral(value,strCan(lang),graph)
        else ldif.entity.Node.createLiteral(value,graph)
      }
      case bno:org.semanticweb.yars.nx.BNode =>
        ldif.entity.Node.createBlankNode(strCan(bno.toString),graph)
      case res:org.semanticweb.yars.nx.Resource =>
        ldif.entity.Node.createUriNode(strCan(res.toString),graph)
    }
  }

  def reconfigure(config: ConfigProperties) {
    val ebType = config.getPropertyValue("entityBuilderType", "in-memory").toLowerCase
    if(ebType=="quad-store")
      useStringPool = false
    else
      useStringPool = true
  }

  def setUseStringPool(on: Boolean) {
    useStringPool = on
  }

  private def strCan(str: String) = if(useStringPool) StringPool.getCanonicalVersion(str) else str
}