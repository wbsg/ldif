package ldif.modules.r2r

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 13.05.11
 * Time: 14:57
 * To change this template use File | Settings | File Templates.
 */

import ldif.module.ModuleConfig
import de.fuberlin.wiwiss.r2r._
import scala.collection.JavaConversions._

class R2RConfig(val ldifMappings: IndexedSeq[LDIFMapping]) extends ModuleConfig {
  def this(repository: Repository) {
    this((for(mapping <- repository.getMappings.values()) yield LDIFMapping(mapping)).toIndexedSeq)
  }
}