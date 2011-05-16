package ldif.modules.r2r

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 13.05.11
 * Time: 14:58
 * To change this template use File | Settings | File Templates.
 */
import ldif.module.ModuleTask
import de.fuberlin.wiwiss.r2r._
import ldif.util.Identifier

class R2RTask(val mapping: LDIFMapping) extends ModuleTask{
  val name: Identifier = mapping.mapping.getUri
}