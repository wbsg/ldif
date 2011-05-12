package ldif.util

import xml.Node

/**
 * Holds namespace prefixes.
 */
class Prefixes(private val prefixMap : Map[String, String])
{
  override def toString = "Prefixes(" + prefixMap.toString + ")"

  /**
   * Combines two prefix objects.
   */
  def ++(prefixes : Prefixes) =
  {
    new Prefixes(prefixMap ++ prefixes.prefixMap)
  }

  def resolve(qualifiedName : String) = qualifiedName.split(":", 2) match
  {
    case Array("http", rest) if rest.startsWith("//") => qualifiedName
    case Array(prefix, suffix) => prefixMap.get(prefix) match
    {
      case Some(resolvedPrefix) => resolvedPrefix + suffix
      case None => throw new IllegalArgumentException("Unknown prefix: " + prefix)
    }
    case _ => throw new IllegalArgumentException("No prefix found in " + qualifiedName)
  }

  def toXML =
  {
    <Prefixes>
    {
      for((key, value) <- prefixMap) yield
      {
        <Prefix id={key} namespace={value} />
      }
    }
    </Prefixes>
  }

  def toSparql =
  {
    var sparql = ""
    for ((key, value) <- prefixMap)
       {
         sparql += "PREFIX "+key+": <"+value +"> "
       }
    sparql
  }

}

object Prefixes
{
  val empty = new Prefixes(Map.empty)

  implicit def fromMap(map : Map[String, String]) = new Prefixes(map)

  implicit def toMap(prefixes : Prefixes) = prefixes.prefixMap

  def apply(map : Map[String, String]) = new Prefixes(map)

  def fromXML(xml : Node) =
  {
    new Prefixes((xml \ "Prefix").map(n => (n \ "@id" text, n \ "@namespace" text)).toMap)
  }

  val stdPrefixes = new Prefixes(Map(
    "xsd" -> "http://www.w3.org/2001/XMLSchema#",
    "rdf" -> "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
    "rdfs" -> "http://www.w3.org/2000/01/rdf-schema#"
  ))

  def resolveStandardQName(qName: String) = stdPrefixes.resolve(qName)
}