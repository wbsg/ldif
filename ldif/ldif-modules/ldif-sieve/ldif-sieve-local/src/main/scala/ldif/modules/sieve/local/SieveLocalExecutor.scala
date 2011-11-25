package ldif.modules.sieve.local

import ldif.module.Executor
import ldif.local.runtime._
import impl.NoEntitiesLeft
import org.apache.commons.io.FileUtils
import ldif.local.util.TemporaryFileCreator
import ldif.modules.sieve.SieveTask
import xml.{XML, Source}
import org.slf4j.LoggerFactory
import java.io.{FileInputStream, File}
import ldif.runtime.Quad
import ldif.util.Prefixes
import ldif.entity.{Node, Entity, EntityDescription}
import ldif.modules.sieve.fusion.{PassItOn, FusionFunction, TrustYourFriends, KeepFirst}

/**
 * Executes Sieve on a local machine.
 * @author pablomendes - based on Silk and R2R executors.
 */
class SieveLocalExecutor(useFileInstanceCache: Boolean = false) extends Executor
{
  private val log = LoggerFactory.getLogger(getClass.getName)

  //private val numThreads = 8
  //private val numThreads = Runtime.getRuntime.availableProcessors

  type TaskType = SieveTask

  type InputFormat = StaticEntityFormat

  type OutputFormat = GraphFormat

  def input(task : SieveTask) : InputFormat =
  {
    implicit val prefixes = task.sieveConfig.sieveConfig.prefixes
    //log.info("Prefixes:"+prefixes.toString)

    //        val entityDescriptions = CreateEntityDescriptions(task.sieveSpec)
    val entityDescriptions = createDummyEntityDescriptions(prefixes)

    new StaticEntityFormat(entityDescriptions)
  }

  def createDummyEntityDescriptions(prefixes: Prefixes) : List[EntityDescription] = {
    // read from jar
    //val stream = getClass.getClassLoader.getResourceAsStream("ldif/modules/sieve/local/Music_EntityDescription.xml")
    //val stream = new FileInputStream("ldif/ldif-modules/ldif-sieve/ldif-sieve-local/src/test/resources/ldif/modules/sieve/local/Music_EntityDescription.xml");

    //if (stream!=null) {
      //val testxml = XML.load(stream);
      val testXml = <EntityDescription>
        <Patterns>
          <Pattern>
            <Path>?a/rdfs:label</Path>
          </Pattern>
        </Patterns>
      </EntityDescription>
      val e = EntityDescription.fromXML(testXml)(prefixes)
      log.info(e.toString);
      List(e)
    //} else {
    //  log.error("EntityDescription returned null!");
    //  List() //empty?
    //}
  }

  def output(task : SieveTask) = new GraphFormat()

  /**
   * Executes a Sieve task.
   */
  override def execute(task : SieveTask, reader : Seq[EntityReader], writer : QuadWriter) {
    log.info("[FUSION] Executing Sieve...")
    val fusionFunction = new PassItOn
    //val fusionFunction = new KeepFirst
    //val fusionFunction = new TrustYourFriends("http://www4.wiwiss.fu-berlin.de/ldif/graph#dbpedia.en");
    reader.foreach( in => {
      var entity : Entity = NoEntitiesLeft;
      while ( { entity = in.read(); entity != NoEntitiesLeft} ) {
        //log.info("Sieve Entity: %s".format(entity.resource.toString))
        val propertyName = "http://www.w3.org/2000/01/rdf-schema#label" //TODO comes from TaskDefinition
        //log.info("Patterns: "+entity.entityDescription.patterns.size)

        //TODO deal with case where there are several patterns
        if (entity!=NoEntitiesLeft)
          fusionFunction.fuse(entity.factums(0)).foreach( n => { // for each property
            if (n.nonEmpty) {
              val propertyValue = n(0) //TODO deal with case where the path is a tree (more than one value)
              val quad = new Quad(entity.resource, propertyName, propertyValue, propertyValue.graph);
              writer.write(quad)
            }
          })

      }
    })
  }
}