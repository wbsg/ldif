package ldif.local.scheduler

import ldif.util.Identifier
import java.io.FileWriter

abstract class ImportJob(val id : Identifier, val changeFreq : String, val dataSource : DataSource) {

  def load(writer : FileWriter)
}
