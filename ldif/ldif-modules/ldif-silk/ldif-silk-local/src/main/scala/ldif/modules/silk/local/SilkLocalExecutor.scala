package ldif.modules.silk.local

import ldif.module.Executor
import ldif.modules.silk.{CreateEntityDescriptions, SilkTask}
import de.fuberlin.wiwiss.silk.util.SourceTargetPair
import de.fuberlin.wiwiss.silk.instance.{Instance, MemoryInstanceCache, InstanceSpecification}
import de.fuberlin.wiwiss.silk.datasource.Source
import de.fuberlin.wiwiss.silk.{OutputTask, FilterTask, MatchTask, LoadTask}
import de.fuberlin.wiwiss.silk.output.Output
import ldif.local.runtime._

/**
 * Executes Silk on a local machine.
 */
class SilkLocalExecutor extends Executor
{
  private val numThreads = 8
//  private val numThreads = Runtime.getRuntime.availableProcessors

  type TaskType = SilkTask

  type InputFormat = StaticEntityFormat

  type OutputFormat = GraphFormat

  def input(task : SilkTask) =
  {
    implicit val prefixes = task.silkConfig.silkConfig.prefixes
    val entityDescriptions = CreateEntityDescriptions(task.linkSpec)

    new StaticEntityFormat(entityDescriptions)
  }

  def output(task : SilkTask) = new GraphFormat()

  /**
   * Executes a Silk task.
   */
  override def execute(task : SilkTask, reader : Seq[EntityReader], writer : QuadWriter)
  {
    val blocking = task.silkConfig.silkConfig.blocking
    val linkSpec = task.linkSpec

    //Retrieve Instance Specifications from Link Specification
    val instanceSpecs = InstanceSpecification.retrieve(linkSpec)

    //Create instance caches
    val caches = SourceTargetPair(
      new MemoryInstanceCache(instanceSpecs.source, blocking.map(_.blocks).getOrElse(1)),
      new MemoryInstanceCache(instanceSpecs.target, blocking.map(_.blocks).getOrElse(1))
    )

    //Load instances into cache
    val sources = SourceTargetPair.fromSeq(reader).map(reader => Source("id", LdifDataSource(reader)))
    def indexFunction(instance: Instance) = linkSpec.condition.index(instance, 0.0)

    val loadTask = new LoadTask(sources, caches, instanceSpecs, if (blocking.isDefined) Some(indexFunction _) else None)
    loadTask()//TODO: for profiling

    //Execute matching
    val matchTask = new MatchTask(linkSpec, caches, numThreads, true)
    val links = matchTask()

    //Filter links
    val filterTask = new FilterTask(links, linkSpec.filter)
    val filteredLinks = filterTask()

    //Write links
    val linkWriter = new LdifLinkWriter(writer)
    val outputTask = new OutputTask(filteredLinks, linkSpec.linkType, Output("output", linkWriter) :: Nil)
    outputTask()
  }
}