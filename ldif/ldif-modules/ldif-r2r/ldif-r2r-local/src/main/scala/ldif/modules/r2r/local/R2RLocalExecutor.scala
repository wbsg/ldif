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
import ldif.local.runtime.{GraphFormat, DynamicEntityFormat, QuadReader, EntityReader}

class R2RLocalExecutor extends Executor {
  type TaskType = R2RTask
  type InputType = GraphForm
  type OutputType = GraphFormat
}