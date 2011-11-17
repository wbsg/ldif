package ldif.modules.r2r.hadoop

import ldif.module.Module
import ldif.modules.r2r.R2RConfig

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 11/17/11
 * Time: 5:24 PM
 * To change this template use File | Settings | File Templates.
 */

class R2RHadoopModule(val config: R2RConfig) extends Module {
  type ConfigType = R2RConfig

  type TaskType = R2RHadoopTask

  def tasks: Traversable[R2RHadoopTask] = {
    List(new R2RHadoopTask(config.ldifMappings))
  }
}