package ldif.local.runtime.impl

import java.io.File
import ldif.entity.{EntityDescription, Entity}
import ldif.local.runtime.EntityReader

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 26.07.11
 * Time: 15:13
 * To change this template use File | Settings | File Templates.
 */

class FileEntityReader(val entityDescription : EntityDescription, inputFile: File) extends FileObjectReader[Entity](inputFile, NoEntitiesLeft) with EntityReader
