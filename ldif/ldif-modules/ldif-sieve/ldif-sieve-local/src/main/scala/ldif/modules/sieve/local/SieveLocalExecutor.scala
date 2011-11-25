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
    val stream = getClass.getClassLoader.getResourceAsStream("ldif/modules/sieve/local/Music_EntityDescription.xml")
    // read from file
    //val stream = new FileInputStream("/home/pablo/workspace/ldif/ldif/ldif-modules/ldif-sieve/ldif-sieve-local/src/test/resources/ldif/modules/sieve/local/Music_EntityDescription.xml");

    if (stream!=null) {
      val testXml = XML.load(stream);
//      val testXml = <EntityDescription>
//        <Patterns>
//          <Pattern>
//            <Path>?a/rdfs:label</Path>
//          </Pattern>
//        </Patterns>
//      </EntityDescription>
      val e = EntityDescription.fromXML(testXml)(prefixes)
      log.debug("[FUSION] "+e.toString);
      List(e)
    } else {
      log.error("EntityDescription returned null!");
      List() //empty?
    }
  }

  def output(task : SieveTask) = new GraphFormat()

  /**
   * Executes a Sieve task.
   */
  override def execute(task : SieveTask, reader : Seq[EntityReader], writer : QuadWriter) {
    log.info("Executing Sieve Task %s".format(task.name))

    // for each entity reader (one per input file?)
    reader.foreach( in => {

      var entity : Entity = NoEntitiesLeft;
      while ( { entity = in.read(); entity != NoEntitiesLeft} ) {
        //log.info("Sieve Entity: %s".format(entity.resource.toString))
        val propertyName = "http://www.w3.org/2000/01/rdf-schema#label" //TODO comes from TaskDefinition
        //log.info("Patterns: "+entity.entityDescription.patterns.size)

        if (entity==null) log.error("Is it normal that some entities will be intermittently null?")

        if (entity!=null && entity!=NoEntitiesLeft) {
          val nPatterns = entity.entityDescription.patterns.size

          for (pattern <- 0 until nPatterns) {
            val factums = entity.factums(pattern)
            val fusionFunction = task.sieveSpec.fusionFunctions(pattern)
            //log.debug("Patern %s: FusionFunction used: %s".format(pattern, fusionFunction))

            fusionFunction.fuse(factums).foreach( n => { // for each property
              if (n.nonEmpty) {
                val propertyValue = n(0) //TODO deal with case where the path is a tree (more than one value)
                val quad = new Quad(entity.resource, propertyName, propertyValue, propertyValue.graph);
                writer.write(quad)
              }
            })
          }
        }
      }
    })

  }

}
