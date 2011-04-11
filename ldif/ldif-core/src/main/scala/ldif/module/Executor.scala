package ldif.module

/**
 * An executor executes a module task.
 */
trait Executor
{
  /**
   * The type of the tasks supported by this executor.
   */
  type TaskType <: ModuleTask

  /**
   * The type of the input format accepted by this executor.
   */
  type InputFormat <: DataFormat

  /**
   * The type of the output format accepted by this executor.
   */
  type OutputFormat <: DataFormat

  /**
   * Determines the accepted input format of a specific task.
   */
  def input(task : TaskType) : InputFormat

  /**
   * Determines the output format of a specific task.
   */
  def output(task : TaskType) : OutputFormat

  /**
   * Executes a specific task.
   *
   * @param task The rask to be executed
   * @param reader The reader of the input data
   * @param writer The writer of the output data
   */
  def execute(task : TaskType, reader : InputFormat#Reader, writer : OutputFormat#Writer)
}

//class SilkExecutor extends Executor
//{
//  type TaskType = ModuleTask
//
//  type InputFormat = StaticInstanceFormat
//
//  type OutputFormat = StaticInstanceFormat
//
//  def input(task : ModuleTask) = StaticInstanceFormat(null)
//
//  def output(task : ModuleTask) = StaticInstanceFormat(null)
//
//  def execute(task : ModuleTask, reader : Seq[CacheReader], writer : Seq[CacheWriter])
//  {
//
//  }
//}