/* 
 * LDIF
 *
 * Copyright 2011-2014 Universität Mannheim, MediaEvent Services GmbH & Co. KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

  override def equals(obj: Any) = {
    obj match {
      case oed: EntityDescription => restriction.equals(oed.restriction) && patterns.equals(oed.patterns)
      case _ => false
    }
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

  def empty = EntityDescription(Restriction(None),IndexedSeq(IndexedSeq.empty[Path]))
}


