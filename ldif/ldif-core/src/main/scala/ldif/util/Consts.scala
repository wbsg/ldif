package ldif.util

import java.text.SimpleDateFormat
import collection.immutable.HashMap

object Consts {
  val DEFAULT_GRAPH = "http://www4.wiwiss.fu-berlin.de/ldif/defaultOutputGraph"
  val DEFAULT_PROVENANCE_GRAPH = "http://www4.wiwiss.fu-berlin.de/ldif/provenance"

  val MAX_WAITING_TIME : Long = Long.MaxValue

  // String pool - for canonicalization
  val POOL_STARTING_SIZE : Int = 200
  val POOL_MAX_SIZE : Int = Int.MaxValue
  val DEFAULT_QUAD_QUEUE_CAPACITY: Int = 1000

  val changeFreqToHours = HashMap(
                "always" -> 0,
                "hourly" -> 1,
                "daily" -> 24,
                "weekly" -> 24 * 7,
                "monthly" -> 24 * 30,
                "yearly" -> 24 * 365,
                "never" ->  Int.MaxValue,
                "onStartup" ->  Int.MaxValue)

  val simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss.S")
  val xsdDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")

  // Import metadata properties
  val lastUpdateProp = "http://www4.wiwiss.fu-berlin.de/ldif/lastUpdate"
  val hasImportJobProp = "http://www4.wiwiss.fu-berlin.de/ldif/hasImportJob"
  val importIdProp = "http://www4.wiwiss.fu-berlin.de/ldif/importId"
  val hasDatasourceProp = "http://www4.wiwiss.fu-berlin.de/ldif/hasDatasource"
  val hasImportTypeProp = "http://www4.wiwiss.fu-berlin.de/ldif/hasImportType"
  val hasOriginalLocationProp = "http://www4.wiwiss.fu-berlin.de/ldif/hasOriginalLocation"
  val rdfTypeProp = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"
  val importedGraphClass = "http://www4.wiwiss.fu-berlin.de/ldif/ImportedGraph"
  val importJobClass = "http://www4.wiwiss.fu-berlin.de/ldif/ImportJob"
}