package ldif.modules.r2r.local

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 13.05.11
 * Time: 14:50
 * To change this template use File | Settings | File Templates.
 */

import ldif.module.Executor
import de.fuberlin.wiwiss.r2r._
import ldif.modules.r2r._
import ldif.local.runtime.{GraphFormat, StaticEntityFormat, QuadWriter, EntityReader}

class R2RLocalExecutor extends Executor {
  type TaskType = R2RTask
  type InputFormat = StaticEntityFormat
  type OutputFormat = GraphFormat

  def input(task: R2RTask) = StaticEntityFormat(Seq(task.mapping.entityDescription))

  def output(task: R2RTask) = new GraphFormat()

  override def execute(task: R2RTask, reader: Seq[EntityReader], writer: QuadWriter) {
    val entityDescription = task.mapping.entityDescription
    val mapping = task.mapping
    val inputQueue = reader.head

    while(inputQueue.hasNext) {
      val entity = reader.head.read
      mapping.executeMapping(entity, writer)
    }
  }
}