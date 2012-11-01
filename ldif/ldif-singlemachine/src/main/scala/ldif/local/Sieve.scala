/*
 * LDIF
 *
 * Copyright 2011-2012 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ldif.local

import rest.MonitorServer
import java.io._
import org.slf4j.LoggerFactory
import ldif.util._
import ldif.config._
import scala.collection.mutable.{Set => MSet}
import scheduler.ImportJob
import xml.{Elem, XML}
import java.util.Properties
import io.Source
import org.apache.commons.io.FileUtils

/**
 * This is a helper for users interested in running only Sieve tasks: QualityAssessment and/or Fusion.
 * It is particularly focusing on the LOD2 Stack use case, where people read/write data from/to SPARQL endpoints.
 * See Ldif and IntegrationJob for running complete workflows.
 *
 * Assumes configuration files are under a directory called "sieve"
 */
object Sieve {

  private val log = LoggerFactory.getLogger(getClass.getName)

  val tmpDir = new File("/tmp/sieve")

  val LIMIT = """(?i).+\sLIMIT\s+(\d+)""".r
  def sparqlParse(endpoint: String, query: String) : Elem = {
    try {
      val body = query.split("\\{")(1).split("\\}")(0)
      val patterns = body.split("\\.").map(_.replaceAll("<","&lt;").replaceAll(">","&gt;"))
      val tripleLimit = query match {
        case LIMIT(limit) => if (limit.toInt>0) limit else ""
        case _ => ""
      }
      <sparqlImportJob>
        <endpointLocation>{ endpoint }</endpointLocation>
        <sparqlPatterns>
          {patterns.map(p => <pattern>{p}</pattern>)}
        </sparqlPatterns>
        <tripleLimit>{tripleLimit}</tripleLimit>
      </sparqlImportJob>
    } catch {
      case e1: ArrayIndexOutOfBoundsException => throw new RuntimeException("Could not parse SPARQL. Forgot \"{\" or \"}\"? ",e1)
      case e2: Exception => throw new RuntimeException("Could not parse SPARQL. ",e2);
    }
  }

  def touch(f: File, xmlContent: Elem) {
    //if (!f.exists()) {
      scala.xml.XML.save(f.getAbsolutePath,xmlContent)
    //} else {
    //  log.warn("Configuration file exists, will not overwrite. Using existing file instead: %s".format(f.getAbsoluteFile))
    //}
  }

  def touch(f: File, propContent: Properties) {
    //if (!f.exists()) {
      propContent.store(new PrintWriter(f),"")
    //} else {
    //  log.warn("Configuration file exists, will not overwrite. Using existing file instead: %s".format(f.getAbsoluteFile))
    //}
  }

  def main(args: Array[String]) {

    if (tmpDir.exists())
      FileUtils.deleteDirectory(tmpDir)

    var debug = false
    var output = ""
    var input = ""
    var query = "SELECT * WHERE { ?s ?p ?o } LIMIT 1000"
    var dir = new File(".").getAbsolutePath
    var dumps = new File("./dumps").getAbsolutePath
    var sieveConfigDir = new File("./sieve").getAbsolutePath
    var outputQualityScores = false

    val parser = new scopt.mutable.OptionParser("Sieve", "0.2") {
      opt("i", "input-endpoint", "<http://localhost:8890/sparql/>", "SPARQL endpoint from which to get the input.", { v: String => input = v })
      opt("o", "output-endpoint", "<file>", "SPARQL endpoint to be used as storage for the resulting triples.", { v: String => output = v })
      opt("d", "dumps", "<dir>", "Directory to be used as local cache of the data send through Sieve.", { v: String => dumps = v })
      opt("c", "config", "<dir>", "Directory with the quality assessment or fusion specs to be used for this job.", { v: String => sieveConfigDir = v })
      opt("q", "query", "<SELECT * WHERE { ?s ?p ?o } LIMIT 1000>", "output is a string property", { v: String => query = v })
      booleanOpt("Q", "quality", "Outputs quality scores.", {  v: Boolean => outputQualityScores = v })
      booleanOpt("v", "debug", "Shows verbose debug messages.", {  v: Boolean => debug = v })
    }
    if (parser.parse(args)) {
      log.info("-------------------------")
      log.info("SIEVE CONFIGURATION")
      log.info("Input: %s".format(input))
      log.info("Output: %s".format(output))
      log.info("Query: %s".format(query))
      log.info("Sieve Specs: %s".format(sieveConfigDir))
      log.info("Cache (dumps) dir: %s".format(dumps))
      log.info("-------------------------")
    }
    else {
      // arguments are bad, usage message will have been displayed
      exit()
    }

    val importJobXml = <importJob xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://ldif.wbsg.de/">
      <internalId>userInput</internalId>
      <dataSource>CommandLineProvided</dataSource>
      <refreshSchedule>monthly</refreshSchedule>
      { sparqlParse(input,query) }
    </importJob>

    val schedulerConfigXml = <scheduler xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://ldif.wbsg.de/ ../xsd/SchedulerConfig.xsd" xmlns="http://ldif.wbsg.de/">
      <properties>{new File(tmpDir,"scheduler.properties").getAbsolutePath}</properties>
      <dataSources>DataSource</dataSources>
      <importJob>{new File(tmpDir,"importJob.xml").getAbsolutePath}</importJob>
      <integrationJob>{new File(tmpDir,"integrationJob.xml").getAbsolutePath}</integrationJob>
      <dumpLocation>{new File(dumps).getAbsolutePath}</dumpLocation>
    </scheduler>

    val integrationJobXml =
      <integrationJob xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://ldif.wbsg.de/ ../xsd/IntegrationJob.xsd" xmlns="http://ldif.wbsg.de/">
        <properties>{new File(tmpDir,"integration.properties").getAbsolutePath}</properties>
        <sources>
          <source>{new File(dumps).getAbsolutePath}</source>
        </sources>
        <linkSpecifications></linkSpecifications>
        <mappings></mappings>
        <sieve>{new File(sieveConfigDir).getAbsolutePath}</sieve>
        <outputs>
          <output>
            <sparql>
              <endpointURI>{output}</endpointURI>
              <sparqlVersion>1.1</sparqlVersion>
              <useDirectPost>true</useDirectPost>
            </sparql>
          </output>
        </outputs>
        <runSchedule>onStartup</runSchedule>
      </integrationJob>

    if (!tmpDir.exists() && !tmpDir.mkdir()) {
      log.error("Could not create tmp dir.")
      throw new IOException("Could not create tmp dir. %s".format(tmpDir.getAbsolutePath))
    }

    val integrationProperties = new Properties
    integrationProperties.load(new StringReader("""discardFaultyQuads=true
                                       validateSources=false
                                       rewriteURIs=true
                                       uriMinting=true
                                       uriMintNamespace=http://new.dbpedia.org/resource/
                                       uriMintLabelPredicate=http://www.w3.org/2000/01/rdf-schema#label
                                       uriMintLanguageRestriction=en fr es
                                       #entityBuilderType=quad-store
                                       outputQualityScores="""+outputQualityScores.toString+"""
                                       qualityFromProvenanceOnly=false
                                       runStatusMonitor=false
                                       #output=fused-only"""))

    val schedulerProperties = new Properties
    schedulerProperties.load(new StringReader("""oneTimeExecution = true
                                        discardFaultyQuads=true""".stripMargin))

    /* I would rather pass the config in memory, but it is not very easy the way things are wired right now.
     * Therefore advice was to create the files and pass them to the scheduler. */
    touch(new File(tmpDir,"importJob.xml"), importJobXml)
    touch(new File(tmpDir,"integrationJob.xml"), integrationJobXml)
    touch(new File(tmpDir,"integration.properties"), integrationProperties)
    touch(new File(tmpDir,"schedulerConfig.xml"), schedulerConfigXml)
    touch(new File(tmpDir,"scheduler.properties"), schedulerProperties)

    val configFile = new File(tmpDir,"schedulerConfig.xml")
    if (!configFile.exists())
       log.error("Could not create scheduler configuration for importing data.")

    // Setup Scheduler
    var config : SchedulerConfig = null
    try {
      config = SchedulerConfig.load(configFile)
    }
    catch {
      case e:ValidationException => {
        log.error("Invalid Scheduler configuration: "+e.toString +
          "\n- More details: " + Consts.xsdScheduler)
        exit()
      }
    }
    val scheduler = Scheduler(config, debug)

    val runStatusMonitor = config.properties.getProperty("runStatusMonitor", Consts.DefaultRunStatusMonitor).toLowerCase=="true"
    val statusMonitorURI = config.properties.getProperty("statusMonitorURI", Consts.DefaultStatusMonitorrURI)

    // Start REST HTTP Server
    if(runStatusMonitor)
      MonitorServer.start(statusMonitorURI)

    // check if dumpDir exists or can be created
    val dumpDir = new File(config.dumpLocationDir)
    if (!dumpDir.exists && !dumpDir.mkdir)  {
      log.error("Dump location doesn't exist and can't be created")
      exit()
    }

    scheduler.run(true, runStatusMonitor)

    FileUtils.deleteDirectory(tmpDir)
  }

  def exit() {
    tmpDir.delete()
    System.exit(1)
  }
}





