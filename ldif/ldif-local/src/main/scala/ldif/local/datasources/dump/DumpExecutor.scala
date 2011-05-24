package ldif.local.datasources.dump

import ldif.datasources.dump.DumpTask
import ldif.module.Executor
import org.semanticweb.yars.nx.parser.NxParser
import ldif.local.runtime.{Quad, GraphFormat, QuadWriter, NoDataFormat}
import org.semanticweb.yars.nx.{Resource, BNode, Literal, Node}

/**
 * Executor for the dump data source.
 */
class DumpExecutor() extends Executor
{
  type TaskType = DumpTask
  type InputFormat = NoDataFormat
  type OutputFormat = GraphFormat

  /**
   * Determines the accepted input format of a specific task.
   */
  override def input(task : DumpTask) : NoDataFormat = NoDataFormat()

  /**
   * Determines the output format of a specific task.
   */
  override def output(task : DumpTask) : GraphFormat = GraphFormat()

  /**
   * Executes a specific task.
   *
   * @param task The task to be executed
   * @param reader The reader of the input data
   * @param writer The writer of the output data
   */
  override def execute(task : DumpTask, reader : Null, writer : QuadWriter)
  {
    val inputStream = new DumpLoader(task.sourceLocation).getStream

    val nxp:NxParser = new NxParser(inputStream)
    while (nxp.hasNext) {
      val ns:Array[Node] = nxp.next
      val graph = "default"
      val subj = mapNode(ns(0),graph)
      val prop = ns(1).toString
      val obj = mapNode(ns(2),graph)
      writer.write(new Quad(subj,prop,obj,graph))
    }
  }

  private def mapNode (node : Node, graph: String) : ldif.entity.Node = {
    node match {
      case lit:Literal => {
        val dt = lit.getDatatype
        val lang = lit.getLanguageTag
        val value = lit.getData
        if (dt!=null)
          ldif.entity.Node.createTypedLiteral(value,dt.toString,graph)
        else if (lang!=null)
          ldif.entity.Node.createLanguageLiteral(value,lang,graph)
        else ldif.entity.Node.createLiteral(value,graph)
      }
      case bno:BNode =>   ldif.entity.Node.createBlankNode(node.toString,graph)
      case res:Resource =>  ldif.entity.Node.createUriNode(node.toString,graph)
    }
  }
}