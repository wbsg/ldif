package ldif.modules.r2r

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 13.05.11
 * Time: 14:58
 * To change this template use File | Settings | File Templates.
 */
import ldif.module.Module
import de.fuberlin.wiwiss.r2r._
import scala.collection.JavaConversions._

class R2RModule(val config: R2RConfig) extends Module {
  type ConfigType = R2RConfig

  type TaskType = R2RTask

  def tasks: Traversable[R2RTask] = { for(mapping <- config.repository.getMappings.values.toIterable) yield new R2RTask(LDIFMapping(mapping))}
}