package ldif.local.scheduler

import ldif.util.Identifier
import java.io.FileWriter
import xml.Node

abstract class ImportJob(val id : Identifier, val refreshSchedule : String, val dataSource : DataSource) {

  def load(writer : FileWriter)

  def generateProvenanceInfo(writer : FileWriter)    {
    //TODO
  }
}

object ImportJob {
  def fromXML(node : Node) : ImportJob = {
    val url = "http://www.assembla.com/code/ldif/git/node/blob/ldif/ldif-singlemachine/src/test/resources/ldif/local/resources/sources/aba.nq.bz2"
    new RDFImportJob(url,"a","weekly",new DataSource(null))
  }
}