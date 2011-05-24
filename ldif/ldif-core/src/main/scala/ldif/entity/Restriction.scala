package ldif.entity

import xml.Elem
import ldif.util.Prefixes

case class Restriction(operator : Option[Restriction.Operator])
{
  def toXml =
  {
    <Restriction>
      { for(op <- operator) yield op.toXml }
    </Restriction>
  }
}

object Restriction
{
  /**
   * Reads a restriction from XML.
   */
  def fromXML(xml : scala.xml.Node)(implicit prefixes : Prefixes = Prefixes.empty) =
  {
    Restriction((xml \ "_").headOption.map(readOperator))
  }

  /**
   * Reads an operator from XML.
   */
  private def readOperator(xml : scala.xml.Node)(implicit prefixes : Prefixes) : Operator = xml match
  {
    case <Condition>{nodes @ _ *}</Condition> => Condition(Path.parse(xml \ "@path" text),(for(node <- nodes if node.text.trim!="") yield Node.fromXML(node)).toSet)
    case <Not>{node}</Not> => Not(readOperator(node))
    case <And>{nodes @ _ *}</And> => And(nodes.map(readOperator))
    case <Or>{nodes @ _ *}</Or> => Or(nodes.map(readOperator))
    case <Exists/> => Exists(Path.parse(xml \ "@path" text))
  }

  sealed trait Operator
  {
    def toXml : Elem
  }

  /**
   * A condition which evaluates to true if the provided path contains at least one of the given values.
   */
  case class Condition(path : Path, values : Set[ldif.entity.Node]) extends Operator
  {
    def toXml =
    {
      <Condition path={path.toString}>
        { values.map(_.toXML) }
      </Condition>
    }
  }

  /**
   * The Exists Operator evaluates to true if the provided path contains at least one value.
   */
  case class Exists(path : Path) extends Operator
  {
    def toXml =
    {
      <Exists path={path.toString} />
    }
  }

  /**
   * Negates the provided operator.
   */
  case class Not(op : Operator) extends Operator
  {
    def toXml = <Not>{op.toXml}</Not>
  }

  /**
   * Evaluates to true if all provided operators evaluate to true.
   */
  case class And(children : Traversable[Operator]) extends Operator
  {
    def toXml = <And>{children.map(_.toXml)}</And>
  }

  /**
   * Evaluates to true if at least one of the provided operators evaluate to true.
   */
  case class Or(children : Traversable[Operator]) extends Operator
  {
    def toXml = <Or>{children.map(_.toXml)}</Or>
  }
}
