package ldif.modules.sieve.fusion

import ldif.entity.{Node, Path, EntityDescription}
import ldif.util.{Consts, Prefixes}
import ldif.entity.Restriction.{Or, Condition}
import java.lang.IllegalArgumentException

/**
 * Helper object to parse entity descriptions out of the Fusion Specification XML
 * @author Hannes Muehleisen
 */

object FusionEntityDescription {

  def getCondition(className: String)(implicit prefixes: Prefixes) = {
    val classNode = Node.createUriNode(prefixes.resolve(className))
    new Condition(Path.parse("?a/<" + Consts.RDFTYPE_URI + ">")(prefixes), Set(classNode))
  }

  /**
   * A <Class> node will be passed in.
   * Create a restriction for the Class (rdf:type)
   * For each sub-element Property, create a pattern.
   */
  def fromXML(node: scala.xml.Node)(implicit prefixes: Prefixes = Prefixes.empty): EntityDescription = {

    val className: String = (node \ "@name").text

    val multipleClasses = className.trim().split(" ")
    if (multipleClasses.size < 1) throw new IllegalArgumentException("You must provide a class name for the <Class> element in a Fusion spec.")

    val condition = if (multipleClasses.size==1) {
      getCondition(className)
    } else {
      Or(multipleClasses.map(getCondition(_)))
    }

    def getOutputProperties(node: scala.xml.Node): IndexedSeq[Path] = {
      IndexedSeq(Path.parse("?a/" + (node \ "@name").text)(prefixes))
    }

    val propertyPaths = (node \ "Property").map(getOutputProperties)
    new EntityDescription(new ldif.entity.Restriction(Option(condition)), propertyPaths.toIndexedSeq)
  }
}