package ldif.entity

import xml.Elem
import ldif.util.Prefixes

case class EntityDescription(restriction : Restriction, patterns : IndexedSeq[IndexedSeq[Path]])
{
  def toXML(implicit prefixes : Prefixes = Prefixes.empty) =
  {
    <EntityDescription>
      { restriction.toXml }
      <Patterns>
      {
        for(pattern <- patterns) yield
        {
          <Pattern>
          {
            for(path <- pattern) yield
            {
              <Path>{path.serialize}</Path>
            }
          }
          </Pattern>
        }
      }
      </Patterns>
    </EntityDescription>
  }
}

object EntityDescription
{
  def fromXML(xml : Elem)(implicit prefixes : Prefixes = Prefixes.empty) =
  {
    EntityDescription(
      restriction = (xml \ "Restriction").headOption match {
        case Some(r) => Restriction.fromXML(r)
        case None => Restriction(None)
      },
      patterns = (xml \ "Patterns").headOption match {
        case Some(p) =>
          for(patternNode <- (p \ "Pattern").toIndexedSeq[scala.xml.Node]) yield
          {
            for(pathNode <- (patternNode \ "Path").toIndexedSeq[scala.xml.Node]) yield
            {
              Path.parse(pathNode.text)
            }
          }
        case None => IndexedSeq(IndexedSeq.empty[Path])
      }
    )
  }
}


