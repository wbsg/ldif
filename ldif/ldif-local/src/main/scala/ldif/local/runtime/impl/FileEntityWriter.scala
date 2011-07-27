package ldif.local.runtime.impl

import java.io.File
import ldif.local.runtime.EntityWriter
import ldif.entity.{EntityDescription, Entity}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 26.07.11
 * Time: 15:18
 * To change this template use File | Settings | File Templates.
 */

class FileEntityWriter(val entityDescription : EntityDescription, val inputFile: File) extends FileObjectWriter[Entity](inputFile, NoEntitiesLeft) with EntityWriter