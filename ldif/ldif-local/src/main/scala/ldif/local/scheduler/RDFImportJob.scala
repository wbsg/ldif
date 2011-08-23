package ldif.local.scheduler

class RDFImportJob(val locationUrl : String, changeFreq : String, dataSource : DataSource) extends ImportJob(locationUrl,changeFreq, dataSource) {

}