package ldif.modules.silk.local

import ldif.module.Executor
import ldif.local.runtime.{QuadWriter, EntityReader, GraphFormat, StaticEntityFormat}
import ldif.modules.silk.{CreateEntityDescriptions, SilkTask}

/**
 * Executes Silk on a local machine.
 */
class SilkLocalExecutor extends Executor
{
  type TaskType = SilkTask

  type InputFormat = StaticEntityFormat

  type OutputFormat = GraphFormat

  def input(task : SilkTask) = new StaticEntityFormat(CreateEntityDescriptions(task.linkSpec))

  def output(task : SilkTask) = new GraphFormat()

  /**
   * Executes a Silk task.
   */
  override def execute(task : SilkTask, reader : Seq[EntityReader], writer : QuadWriter)
  {
    //TODO
  }
}