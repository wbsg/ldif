package ldif.local.scheduler

import java.io.{FileWriter, InputStreamReader, BufferedReader}
import ldif.local.datasources.dump.DumpLoader

class RDFImportJob(val locationUrl : String, changeFreq : String, dataSource : DataSource) extends ImportJob(locationUrl,changeFreq, dataSource) {

  override def load(writer : FileWriter) {

    // get bufferReader from Url
    val inputStream = new DumpLoader(locationUrl).getStream
    val bufferedReader = new BufferedReader(new InputStreamReader(inputStream))

    // use job.id as graph name
    //TODO

    // parse and write to file
    //TODO
    //val quadParser = new QuadFileLoader(id)
    //quadParser.readQuads(bufferedReader, writer)
  }
}