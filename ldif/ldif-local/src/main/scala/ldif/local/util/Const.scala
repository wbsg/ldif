package ldif.local.util

import collection.mutable.HashMap

object Const{

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
                "never" ->  Int.MaxValue)

  val DEFAULT_PROVENANCE_GRAPH = "http://www4.wiwiss.fu-berlin.de/ldif/provenance"
  val DEFAULT_DUMP_LOCATION = "dumps"

}