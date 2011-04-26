package ldif.datasources.dump

import ldif.module.ModuleTask
import ldif.util.Identifier

class DumpTask(override val name : Identifier, val dataLocationUrl : String) extends ModuleTask