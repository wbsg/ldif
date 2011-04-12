package ldif.resource

import xml.Elem

case class Restriction(operator : Restriction.Operator)
{
  //TODO
  def toSparql = ""

  def toXml =
  {
    <Restriction>
      { operator.toXml }
    </Restriction>
  }
}

object Restriction
{
  sealed trait Operator
  {
    def toXml : Elem
  }

  /**
   * A condition which evaluates to true if the provided path contains at least one of the given values.
   */
  case class Condition(path : Path, values : Set[String]) extends Operator
  {
    def toXml =
    {
      <Condition path={path.toString}>
        { values.map(v => <Value>{v}</Value>) }
      </Condition>
    }
  }

  /**
   * Negates the provided operator.
   */
  case class Not(op : Operator)
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
}