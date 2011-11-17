package ldif.modules.r2r.hadoop

import java.util.logging.Logger
import ldif.modules.r2r.R2RTask
import ldif.hadoop.runtime._
import org.apache.hadoop.fs.Path
import ldif.module.Executor

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 11/16/11
 * Time: 2:39 PM
 * To change this template use File | Settings | File Templates.
 */

class R2RHadoopExecutor extends Executor {
  private val log = Logger.getLogger(getClass.getName)
  type TaskType = R2RHadoopTask
  type InputFormat = EntityFormat
  type OutputFormat = QuadFormat

  def input(task: R2RHadoopTask) = EntityFormat(for(mapping <- task.ldifMappings) yield mapping.entityDescription)

  def output(task: R2RHadoopTask) = new QuadFormat()

  override def execute(task: R2RHadoopTask, reader: Seq[Path], writer: Path) {
    val mappings = task.ldifMappings

    //Run Hadoop Job with given input and output path
  }
}