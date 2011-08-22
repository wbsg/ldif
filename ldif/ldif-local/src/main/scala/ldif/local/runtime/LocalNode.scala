package ldif.local.runtime

import ldif.entity.Node
import ldif.local.util.StringPool

/*
 * Object used to create Node using string canonicalization
 */

object LocalNode
{
  private var useStringPool = true

  def createResourceNode(value : String, graph : String) = {
    if (!value.startsWith("_:") && !value.startsWith("<")) {
      // Add brackets to SPARQL result URIs
      Node.fromString("<"+strCan(value)+">", strCan(graph))
    }
    else
      Node.fromString(strCan(value), strCan(graph))
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

  def intern(node : Node) = {
    node.copy(strCan(node.value), strCan(node.datatypeOrLanguage), node.nodeType, strCan(node.graph))
  }

  private def strCan(str: String) = if(useStringPool) StringPool.getCanonicalVersion(str) else str
}