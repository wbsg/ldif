package ldif.resource

case class Restriction(operator : Restriction.Operator)
{
  //TODO
  def toSparql = ""
}

object Restriction
{
  sealed trait Operator

  /**
   * A condition which evaluates to true if the provided path contains at least one of the given values.
   */
  case class Condition(path : Path, values : Set[String]) extends Operator

  /**
   * Negates the provided operator.
   */
  case class Not(op : Operator)

  /**
   * Evaluates to true if all provided operators evaluate to true.
   */
  case class And(children : Traversable[Operator]) extends Operator
}