package ldif.modules.r2r.hadoop

import ldif.module.ModuleTask
import ldif.util.Identifier
import de.fuberlin.wiwiss.r2r.LDIFMapping

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 11/17/11
 * Time: 5:20 PM
 * To change this template use File | Settings | File Templates.
 */

class R2RHadoopTask(val ldifMappings: IndexedSeq[LDIFMapping]) extends ModuleTask{
  val name: Identifier = ""
}