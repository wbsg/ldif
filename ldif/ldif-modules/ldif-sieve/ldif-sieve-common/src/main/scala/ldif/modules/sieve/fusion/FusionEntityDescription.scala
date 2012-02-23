package ldif.modules.sieve.fusion

import ldif.entity.Restriction.Condition
import ldif.entity.{Node, Path, EntityDescription}
import ldif.util.{Consts, Prefixes}

/**
 * Helper object to parse entity descriptions out of the Fusion Specification XML
 * @author Hannes Muehleisen
 */

object FusionEntityDescription {

  /**
   * A <Class> node will be passed in.
   * Create a restriction for the Class (rdf:type)
   * For each sub-element Property, create a pattern.
   */
  def fromXML(node: scala.xml.Node)(implicit prefixes: Prefixes = Prefixes.empty): EntityDescription = {

    val className: String = (node \ "@name").text
    val classNode = Node.createUriNode(prefixes.resolve(className))

    val condition = new Condition(Path.parse("?a/<" + Consts.RDFTYPE_URI + ">")(prefixes), Set(classNode))

    def getOutputProperties(node: scala.xml.Node): IndexedSeq[Path] = {
      IndexedSeq(Path.parse("?a/" + (node \ "@name").text)(prefixes))
    }

    val propertyPaths = (node \ "Property").map(getOutputProperties)
    new EntityDescription(new ldif.entity.Restriction(Option(condition)), propertyPaths.toIndexedSeq)
  }
}