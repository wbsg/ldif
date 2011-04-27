package ldif.datasources.dump

import ldif.module.ModuleTask
import ldif.util.Identifier

class DumpTask(override val name : Identifier, val sourceLocation : String) extends ModuleTask