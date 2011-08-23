package ldif.local.scheduler

class NQuadImportJob(val locationUrl : String, changeFreq : String, dataSource : DataSource) extends ImportJob(locationUrl, changeFreq, dataSource) {

}