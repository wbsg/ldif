package ldif.local.scheduler

import ldif.util.Identifier

abstract class ImportJob(val id : Identifier, val changeFreq : String, val dataSource : DataSource) {

}
