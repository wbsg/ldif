package ldif.local.runtime.impl

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import ldif.local.datasources.sparql.SparqlExecutor
import ldif.datasources.sparql.{SparqlModule, SparqlConfig}
import ldif.entity.EntityDescription
import xml.XML
import ldif.util.Prefixes

@RunWith(classOf[JUnitRunner])
class SparqlExecutorTest extends FlatSpec with ShouldMatchers {

  val sourceUrl = "http://cheminfov.informatics.indiana.edu:8080/pharmgkb/sparql"

  val executor = new SparqlExecutor
  val eq = task.entityDescriptions.map(new EntityQueue(_))

  it should "build entities correctly" in {
    executor.execute(task,null,eq)
    eq.head.size should equal (1000)
  }

  private lazy val task = {
    val config = new SparqlConfig(Traversable(sourceUrl), IndexedSeq(ed("pharmGKB_ed.xml")))
    val module = new SparqlModule(config)
    module.tasks.head
  }

  private def ed(sourceUrl : String) = {

    implicit val prefixes = Prefixes(Map(
      "rdf" -> "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
      "pharmGKB" -> "http://chem2bio2rdf.org/pharmgkb/resource/"))

    val stream = getClass.getClassLoader.getResourceAsStream(sourceUrl)

    EntityDescription.fromXML(XML.load(stream))
  }

}