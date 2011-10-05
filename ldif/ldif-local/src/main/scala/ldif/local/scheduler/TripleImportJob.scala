package ldif.local.scheduler

import ldif.local.datasources.dump.DumpLoader
import ldif.util.Identifier
import xml.Node
import ldif.datasources.dump.QuadParser
import java.io.{OutputStreamWriter, OutputStream}

case class TripleImportJob(dumpLocation : String, id : Identifier, refreshSchedule : String, dataSource : String) extends ImportJob {

  override def load(out : OutputStream) : Boolean = {

    val writer = new OutputStreamWriter(out)

    // get bufferReader from Url
    val inputStream = DumpLoader.getStream(dumpLocation)
    //val bufferedReader = new BufferedReader(new InputStreamReader(inputStream))

    val graph = id
    importedGraphs += graph

    val parser = new QuadParser(graph)
    val lines = scala.io.Source.fromInputStream(inputStream).getLines
    for (line <- lines.toTraversable){
        val quad = parser.parseLine(line)
        if (quad != null)
          writer.write(quad.toNQuadFormat+". \n")
    }
    writer.flush
    writer.close
    true
  }

  override def getType = "triple"
  override def getOriginalLocation = dumpLocation
}

object TripleImportJob {

  def fromXML (node : Node, id : Identifier, refreshSchedule : String, dataSource : String) : ImportJob = {
    val dumpLocation : String = (node \ "dumpLocation") text
    val job = new TripleImportJob(dumpLocation.trim, id, refreshSchedule, dataSource)
    job
  }
}