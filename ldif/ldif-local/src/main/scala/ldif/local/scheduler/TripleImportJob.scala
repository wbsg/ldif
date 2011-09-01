package ldif.local.scheduler

import ldif.local.datasources.dump.DumpLoader
import ldif.util.Identifier
import xml.Node
import java.io.Writer
import ldif.datasources.dump.QuadParser

case class TripleImportJob(dumpLocation : String, id : Identifier, refreshSchedule : String, dataSource : String) extends ImportJob {

  override def load(writer : Writer) {

    // get bufferReader from Url
    val inputStream = new DumpLoader(dumpLocation).getStream
    //val bufferedReader = new BufferedReader(new InputStreamReader(inputStream))

    val graph = dataSource+"_"+id
    importedGraphs += graph

    val parser = new QuadParser(graph)
    val lines = scala.io.Source.fromInputStream(inputStream).getLines
    for (line <- lines.toTraversable){
        val quad = parser.parseLine(line)
        writer.write(quad.toNQuadFormat+". \n")
    }
    writer.flush
    writer.close
  }

  override def getType = "triple"
}

object TripleImportJob {

  def fromXML (node : Node, id : Identifier, refreshSchedule : String, dataSource : String) : ImportJob = {
    val dumpLocation : String = (node \ "dumpLocation") text
    val job = new TripleImportJob(dumpLocation.trim, id, refreshSchedule, dataSource)
    job
  }
}