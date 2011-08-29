package ldif.local.scheduler

import ldif.local.datasources.dump.DumpLoader
import java.io.{FileWriter, InputStreamReader, BufferedReader}

class NQuadImportJob(val locationUrl : String, changeFreq : String, dataSource : DataSource) extends ImportJob(locationUrl, changeFreq, dataSource) {

  override def load(writer : FileWriter) {

    // get bufferReader from Url
    val inputStream = new DumpLoader(locationUrl).getStream
    val bufferedReader = new BufferedReader(new InputStreamReader(inputStream))

    // define graph names rewriting rule
    //TODO

    // parse and write to file
    //TODO
    //val quadParser = new QuadFileLoader("...")
    //quadParser.readQuads(bufferedReader, writer)
  }

}