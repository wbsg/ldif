package ldif.local.scheduler

import ldif.local.datasources.dump.DumpLoader
import ldif.util.Identifier
import xml.Node
import ldif.datasources.dump.QuadParser
import java.io.{OutputStreamWriter, OutputStream}

case class QuadImportJob(dumpLocation : String, id : Identifier, refreshSchedule : String, dataSource : String) extends ImportJob {

  override def load(out : OutputStream) {

    val writer = new OutputStreamWriter(out)

    // get bufferReader from Url
    val inputStream = new DumpLoader(dumpLocation).getStream
    //val bufferedReader = new BufferedReader(new InputStreamReader(inputStream))

    val parser = new QuadParser
    val lines = scala.io.Source.fromInputStream(inputStream).getLines
    for (line <- lines.toTraversable){
        val quad = parser.parseLine(line)
        importedGraphs += quad.graph
        writer.write(quad.toNQuadFormat+" . \n")
    }
    writer.flush
    writer.close
  }

  override def getType = "quad"
  override def getOriginalLocation = dumpLocation
}

object QuadImportJob{

  def fromXML (node : Node, id : Identifier, refreshSchedule : String, dataSource : String) : ImportJob = {
    val dumpLocation : String = (node \ "dumpLocation") text
    val job = new QuadImportJob(dumpLocation.trim, id, refreshSchedule, dataSource)
    job
  }
}