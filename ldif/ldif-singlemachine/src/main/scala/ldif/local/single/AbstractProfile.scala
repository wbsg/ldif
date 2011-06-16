package ldif.local.single

import de.fuberlin.wiwiss.ldif.local.EntityBuilder

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 15.06.11
 * Time: 16:27
 * To change this template use File | Settings | File Templates.
 */

abstract class AbstractProfile {
  val threadedDumpLoad: Boolean
  val threadedCreateHTs: Boolean
  val entityBuilder: Class[EntityBuilder]
}

class DefaultProfile extends AbstractProfile {
  val threadedDumpLoad = false
  val threadedCreateHTs = false
  val entityBuilder = classOf[EntityBuilder]
}