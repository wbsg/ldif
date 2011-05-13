package ldif.modules.silk.local

import ldif.module.Executor
import ldif.local.runtime.{QuadWriter, EntityReader, GraphFormat, StaticEntityFormat}
import ldif.modules.silk.{CreateEntityDescriptions, SilkTask}
import de.fuberlin.wiwiss.silk.util.{Future, SourceTargetPair}
import de.fuberlin.wiwiss.silk.instance.{Instance, MemoryInstanceCache, FileInstanceCache, InstanceSpecification}
import de.fuberlin.wiwiss.silk.datasource.Source
import de.fuberlin.wiwiss.silk.impl.writer.MemoryWriter
import de.fuberlin.wiwiss.silk.{OutputTask, FilterTask, MatchTask, LoadTask}
import de.fuberlin.wiwiss.silk.output.Output
import ldif.entity.{FactumRow, FactumTable, EntityDescription, Entity}

/**
 * Executes Silk on a local machine.
 */
class SilkLocalExecutor extends Executor
{
  private val numThreads = 4

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
    val blocking = task.silkConfig.blocking
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
    def blockingFunction(instance : Instance) = linkSpec.condition.index(instance, linkSpec.filter.threshold).map(_ % blocking.map(_.blocks).getOrElse(1))

    val loadTask = new LoadTask(sources, caches, instanceSpecs, if(blocking.isDefined) Some(blockingFunction _) else None)
    val loader = loadTask.runInBackground()

    //Execute matching
    val matchTask = new MatchTask(linkSpec, caches, numThreads)
    val links = matchTask()

    //Filter links
    val filterTask = new FilterTask(links, linkSpec.filter)
    val filteredLinks = filterTask()

    //Write links
    val writer = new MemoryWriter()

    val outputTask = new OutputTask(filteredLinks, linkSpec.linkType, Output(writer) :: Nil)
    outputTask()

    //Write output
    val linkMap = writer.links.map(link => (link.targetUri, link.sourceUri)).toMap
  }
}