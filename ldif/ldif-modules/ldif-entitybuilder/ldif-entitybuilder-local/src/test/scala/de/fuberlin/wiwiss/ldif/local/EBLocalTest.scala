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
  val source = "src/test/resources/aba.nt"
  val eds = IndexedSeq(ed1,ed2,ed3)

  // init queue structures
  val qq = new QuadQueue
  loadQuads

  val eqs = new Array[EntityQueue](eds.size)
  for ((ed, i) <- eds.zipWithIndex)
     eqs(i) = new EntityQueue(ed)  

  val ebe = new EntityBuilderExecutor
  
  ebe.execute(task, qq.reader, eqs.map(x => x.writer))





  "EBLocal" should "create the correct number of entities" in
  {
    eqs(0).reader.size should equal (5)
    eqs(1).reader.size should equal (4)
    eqs(2).reader.size should equal (5)
  }



  "EBLocal" should "retrieve the correct number of factum rows" in
  {
    while(!eqs(0).reader.isEmpty){
      eqs(0).reader.read.factums(0).size should equal (1)
    }
    while(!eqs(1).reader.isEmpty){
      val entity = eqs(1).reader.read
      if (entity.uri == "<http://brain-map.org/mouse/brain/Chrna4.xml>")
        entity.factums(0).size should equal (5)
      if (entity.uri == "<http://brain-map.org/mouse/brain/Chrnb2.xml>")
        entity.factums(0).size should equal (5)
      if (entity.uri == "<http://brain-map.org/mouse/brain/Chrna7.xml>")
        entity.factums(0).size should equal (3)
    }
    while(!eqs(2).reader.isEmpty){
      val entity = eqs(2).reader.read
      if (entity.uri == "<http://brain-map.org/mouse/brain/Chrna4.xml>")
        entity.factums(0).size should equal (3)
      if (entity.uri == "<http://brain-map.org/mouse/brain/Chrnb2.xml>")
        entity.factums(0).size should equal (3)
      if (entity.uri == "<http://brain-map.org/mouse/brain/Chrna7.xml>")
        entity.factums(0).size should equal (2)
    }
  }

  private lazy val loadQuads =  {
    val dlc = new DumpConfig(List(source))
    val dlm = new DumpModule(dlc)
    val dle = new DumpExecutor
    for (dlt <- dlm.tasks)
      dle.execute(dlt,null,qq.writer)
  }

  private lazy val task = {
    val ebc = new EntityBuilderConfig(IndexedSeq(ed1,ed2,ed3))
    val ebm = new EntityBuilderModule(ebc)
    // eb has only one task
    ebm.tasks.head
  }


  private lazy val ed1 = {
         /*  mp:Gene
       a r2r:ClassMapping;
       r2r:prefixDefinitions	"""smwcat: <http://halowiki/ob/category#> .
                     smwprop: <http://halowiki/ob/property#> .
                     aba: <http://brain-map.org/gene/0.1#> .
                     uniprot: <http://purl.uniprot.org/core/> .
                     skos: <http://www.w3.org/2004/02/skos/core#> .
                     rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
                   xsd: <http://www.w3.org/2001/XMLSchema#>""";
       r2r:sourcePattern 	"?SUBJ a aba:gene";
       r2r:targetPattern	"?SUBJ a smwcat:Gene";*/

     /* Properties of Gene
      mp:Geneid
         a r2r:PropertyMapping;
         r2r:mappingRef    mp:Gene;
         r2r:sourcePattern 	"?SUBJ aba:geneid ?x";
         r2r:targetPattern	"?SUBJ smwprop:AbaGeneId ?'x'^^xsd:string";	*/

    val pathRest = Path("SUBJ",List(ForwardOperator(new Uri("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>"))))
    val cond = Some(Condition(pathRest,Set("<http://brain-map.org/gene/0.1#gene>")))
    val rest = Restriction(cond)

    val pathValue = Path ("SUBJ",List(ForwardOperator(new Uri("<http://brain-map.org/gene/0.1#geneid>"))))

    new EntityDescription(rest,IndexedSeq(IndexedSeq(pathValue)))
  }
  
  private lazy val ed2 = {
    // ed1
       // +	Condition: ?SUBJ /<http://brain-map.org/gene/0.1#projectname>  = "0310"
       // +  Pattern path: ?SUBJ aba:genename ?x
       // +  Pattern path: ?SUBJ aba:gene-aliases ?x . ?x aba:aliassymbol ?s

    val pathCond1 = Path("SUBJ",List(ForwardOperator(new Uri("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>"))))
    val cond1 = Condition(pathCond1,Set("<http://brain-map.org/gene/0.1#gene>"))
    val pathCond2 = Path("SUBJ",List(ForwardOperator(new Uri("<http://brain-map.org/gene/0.1#projectname>"))))
    val cond2 = Condition(pathCond2,Set("\"0310\""))
    val and = Some(And(Seq(cond1,cond2)))
    val rest = Restriction(and)

    val pathValue1 = Path ("SUBJ",List(ForwardOperator(new Uri("<http://brain-map.org/gene/0.1#geneid>"))))
    val pathValue2 = Path ("SUBJ",List(ForwardOperator(new Uri("<http://brain-map.org/gene/0.1#genename>"))))
    //val pathValue3 = Path.parse("?SUBJ/<http://brain-map.org/gene/0.1#gene-aliases>")
    val pathValue3 = Path ("SUBJ",List(ForwardOperator(new Uri("<http://brain-map.org/gene/0.1#gene-aliases>")),ForwardOperator(new Uri("<http://brain-map.org/gene/0.1#aliassymbol>"))))

    new EntityDescription(rest,IndexedSeq(IndexedSeq(pathValue1,pathValue2,pathValue3)))
  }

  private lazy val ed3 = {
    // ed1
       // +	Pattern path: ?SUBJ aba:image-series ?x . ?x aba:imageseriesid ?a
       // +	Pattern path: ?SUBJ aba:image-series ?x . ?x aba:riboprobename ?b

   // Path.parse
    val pathCond1 = Path("SUBJ",List(ForwardOperator(new Uri("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>"))))
    val cond = Some(Condition(pathCond1,Set("<http://brain-map.org/gene/0.1#gene>")))
    val rest = Restriction(cond)

    val pathValue1 = Path ("SUBJ",List(ForwardOperator(new Uri("<http://brain-map.org/gene/0.1#geneid>"))))
    val pathValue2 = Path ("SUBJ",List(ForwardOperator(new Uri("<http://brain-map.org/gene/0.1#image-series>")),ForwardOperator(new Uri("<http://brain-map.org/gene/0.1#imageseriesid>"))))
    val pathValue3 = Path ("SUBJ",List(ForwardOperator(new Uri("<http://brain-map.org/gene/0.1#image-series>")),ForwardOperator(new Uri("<http://brain-map.org/gene/0.1#riboprobename>"))))

    new EntityDescription(rest,IndexedSeq(IndexedSeq(pathValue1, pathValue2, pathValue3)))
  }
  
  private def ed(url : String) = {
    val stream = getClass.getClassLoader.getResourceAsStream(url)
    EntityDescription.fromXML(XML.load(stream))
  }
}