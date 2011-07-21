package de.fuberlin.wiwiss.ldif.local.tdb

import com.hp.hpl.jena.tdb.store.bulkloader2.{CmdIndexBuild, CmdNodeTableBuilder}
import java.io._

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 05.07.11
 * Time: 11:24
 * To change this template use File | Settings | File Templates.
 */

class TDBLoader {
  private val k1 = "-k 1,1"
  private val k2 = "-k 2,2"
  private val k3 = "-k 3,3"
  private val k4 = "-k 4,4"


//  def generateStatistics(rootDir: String): Unit = {
//    val statsFile = new File(rootDir, "stats.opt")
//    val oldOut = System.out
//    System.setOut(
//      new PrintStream(
//        new BufferedOutputStream(
//          new FileOutputStream(statsFile))));
//
//    tdb.tdbconfig.main("stats", "--loc " + rootDir)
//    System.setOut(oldOut)
//  }

  private def indexData(rootDir: String, datatriples: String, dataquads: String): Unit = {
    processRows(rootDir, makeRowString(k1, k2, k3), datatriples, "SPO")
    processRows(rootDir, makeRowString(k2, k3, k1), datatriples, "POS")
    processRows(rootDir, makeRowString(k3, k1, k2), datatriples, "OSP")
// The following indexes will never be used in out queries
//    processRows(rootDir, makeRowString(k1, k2, k3, k4), dataquads, "GSPO")
//    processRows(rootDir, makeRowString(k1, k3, k4, k2), dataquads, "GPOS")
//    processRows(rootDir, makeRowString(k1, k4, k2, k3), dataquads, "GOSP")
    processRows(rootDir, makeRowString(k2, k3, k4, k1), dataquads, "SPOG")
    processRows(rootDir, makeRowString(k3, k4, k2, k1), dataquads, "POSG")
    processRows(rootDir, makeRowString(k4, k2, k3, k1), dataquads, "OSPG")
  }

  private def loadData(rootDir: String, datatriples: String, dataquads: String, dataFile: String): Unit = {
    CmdNodeTableBuilder.main("--loc=" + rootDir, "--triples=" + datatriples, "--quads=" + dataquads, dataFile)
  }

  def createNewTDBDatabase(databaseRoot: String, datasetFile: String) {
    val time1 = System.currentTimeMillis
    val dataTriples = databaseRoot + "/data-triples.tmp"
    val dataQuads = databaseRoot + "/data-quads.tmp"
    val dataFile = datasetFile
    val dt = new File(dataTriples)
    val dq = new File(dataQuads)
    dt.createNewFile
    dt.delete
    dt.createNewFile
    dq.createNewFile
    dq.delete
    dq.createNewFile


    println("-- TDB Bulk Loader Start")
    println("-- Data phase")
    cleanTarget(databaseRoot)

    loadData(databaseRoot, dataTriples, dataQuads, dataFile)

    println("-- Index phase")
    indexData(databaseRoot, dataTriples, dataQuads)

    // Calculate overall processing time
    val timeSpan = System.currentTimeMillis - time1
    println("-- TDB Bulk Loader Finish")
    println("-- " + String.format("%.2f", (timeSpan/1000.0).asInstanceOf[AnyRef]) + "s\n")

    // Clean up
    dt.delete
    dq.delete
  }

  /**
   * Remove all files from databaseRoot directory
   */
  def cleanTarget(databaseRoot: String) {
    val databaseDir = new File(databaseRoot)
    if(!databaseDir.isDirectory)
      throw new IllegalArgumentException(databaseDir.getAbsolutePath + " is no directory!")
    for(file <- databaseDir.listFiles)
      file.delete
  }

  private def processRows(rootDir: String, rowString: String, file: String, index: String) {
    val keys = rowString
    val data = file
    if(new File(data).length==0)
      return

    val idx = index
    val work = rootDir + "/" + idx + "-txt"
    new File(work).createNewFile

    val sortString = "sort -u " + keys + " < " + data + " > " + work

    println("Index " + idx)
    val returnCode = executeCommand(sortString)

    new File(rootDir, idx + ".dat").delete
    new File(rootDir, idx + ".idn").delete

    println("Build " + idx)
    CmdIndexBuild.main(rootDir, idx, work)

    // Clean up
    new File(work).delete
  }

  private def executeCommand(command: String) {
    try {
      val process = Runtime.getRuntime().exec(Array[String]("/bin/sh", "-c",command));
      val in = new BufferedReader(new InputStreamReader(process.getInputStream()));
      var line: String = in.readLine;

      while (line != null) {
        System.out.println(line);
        line = in.readLine
      }
      val returnCode = process.waitFor()
      if(returnCode!=0)
        throw new RuntimeException("Error while loading dataset into TDB for command: " + command)
    } catch {
      case e => throw new RuntimeException("Error while loading dataset into TDB for command: " + command, e)
    }
  }

  private def makeRowString(k: String*): String = {
    val sb = new StringBuilder

    for(s <- k)
      sb.append(s).append(" ")

    sb.toString.trim
  }
}