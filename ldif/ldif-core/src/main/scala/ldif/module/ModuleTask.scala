package ldif.module

import ldif.util.Identifier

/**
 * A Module Task.
 */
trait ModuleTask
{
  /**
   * The unique name of this task.
   */
  val name : Identifier

  /**
   * Default toString method.
   */
  override def toString = getClass.getSimpleName + "(" + name + ")"
}
