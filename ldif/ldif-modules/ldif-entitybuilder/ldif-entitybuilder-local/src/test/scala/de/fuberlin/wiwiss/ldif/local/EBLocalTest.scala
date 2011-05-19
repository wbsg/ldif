package de.fuberlin.wiwiss.ldif.local

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import xml.XML
import ldif.entity.ForwardOperator
import ldif.entity._
import ldif.datasources.dump.{DumpModule, DumpConfig}
import ldif.entity.Restriction.{And, Condition}
import ldif.local.runtime.impl.{QuadQueue, EntityQueue}
import ldif.local.datasources.dump.DumpExecutor
import de.fuberlin.wiwiss.ldif.{EntityBuilderModule, EntityBuilderConfig}
import ldif.util.{Prefixes, Uri}
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith

/**
 * Unit Test for the Entity Builder Module Local.
 */

@RunWith(classOf[JUnitRunner])
class EBLocalTest extends FlatSpec with ShouldMatchers
{
  // context
  val source = getClass.getClassLoader.getResource("aba.nt")
  val eds = IndexedSeq(ed("aba_ed_1.xml"),ed("aba_ed_2.xml"),ed("aba_ed_3.xml"))

  // init queue structures
  val qq = new QuadQueue
  loadQuads

  val eqs = new Array[EntityQueue](eds.size)
  for ((ed, i) <- eds.zipWithIndex)
     eqs(i) = new EntityQueue(ed)  

  val ebe = new EntityBuilderExecutor
  
  ebe.execute(task, qq.reader, eqs.map(x => x.writer))

//  "DumpLoader" should "read the correct number of quads" in {
//    loadQuads
//    qq.reader.size should equal (1232)
//  }

  "EBLocal" should "create the correct number of entities" in  {
    eqs(0).reader.size should equal (5)
    eqs(1).reader.size should equal (4)
    eqs(2).reader.size should equal (5)
  }

  "EBLocal" should "retrieve the correct number of factum rows" in  {
    while(eqs(0).reader.hasNext){
      eqs(0).reader.read.factums(0).size should equal (1)
    }
    while(eqs(1).reader.hasNext){
      val entity = eqs(1).reader.read
      if (entity.uri == "http://brain-map.org/mouse/brain/Chrna4.xml")
        entity.factums(0).size should equal (5)
      if (entity.uri == "http://brain-map.org/mouse/brain/Chrnb2.xml")
        entity.factums(0).size should equal (5)
      if (entity.uri == "http://brain-map.org/mouse/brain/Chrna7.xml")
        entity.factums(0).size should equal (3)
    }
    while(eqs(2).reader.hasNext){
      val entity = eqs(2).reader.read
      if (entity.uri == "http://brain-map.org/mouse/brain/Chrna4.xml")
        entity.factums(0).size should equal (3)
      if (entity.uri == "http://brain-map.org/mouse/brain/Chrnb2.xml")
        entity.factums(0).size should equal (3)
      if (entity.uri == "http://brain-map.org/mouse/brain/Chrna7.xml")
        entity.factums(0).size should equal (2)
    }
  }

  private lazy val loadQuads =  {
    val dlc = new DumpConfig(List(source.toString))
    val dlm = new DumpModule(dlc)
    val dle = new DumpExecutor
    for (dlt <- dlm.tasks)
      dle.execute(dlt,null,qq.writer)
  }

  private lazy val task = {
    val ebc = new EntityBuilderConfig(eds)
    val ebm = new EntityBuilderModule(ebc)
    // eb has only one task
    ebm.tasks.head
  }

  private def ed(sourceUrl : String) = {

    implicit val prefixes = Prefixes(Map(
      "rdf" -> "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
      "aba" -> "http://brain-map.org/gene/0.1#"))

    val stream = getClass.getClassLoader.getResourceAsStream(sourceUrl)

    EntityDescription.fromXML(XML.load(stream))
  }

}